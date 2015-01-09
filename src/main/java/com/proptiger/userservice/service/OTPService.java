package com.proptiger.userservice.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.mail.MailSender;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.Application;
import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.model.CompanyIP;
import com.proptiger.core.model.proptiger.CompanySubscription;
import com.proptiger.core.model.proptiger.UserSubscriptionMapping;
import com.proptiger.core.pojo.LimitOffsetPageRequest;
import com.proptiger.core.repo.APIAccessLogDao;
import com.proptiger.core.util.IPUtils;
import com.proptiger.core.util.PropertyKeys;
import com.proptiger.core.util.PropertyReader;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.config.security.AuthSuccessHandler;
import com.proptiger.userservice.model.UserOTP;
import com.proptiger.userservice.repo.CompanyIPDao;
import com.proptiger.userservice.repo.UserOTPDao;
import com.proptiger.userservice.util.OTPGenerator;

/**
 * Service class to handle generation/validation of one time password.
 * 
 * @author Rajeev Pandey
 *
 */
public class OTPService {

    @Autowired
    private UserOTPDao              userOTPDao;

    private OTPGenerator            generator = new OTPGenerator();

    @Autowired
    private AuthSuccessHandler      authSuccessHandler;

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    @Autowired
    private CompanyIPDao            companyIPDao;
    
    public boolean isOTPRequired(Authentication auth, HttpServletRequest request) {
        boolean required = false;
        if(!PropertyReader.getRequiredPropertyAsType(PropertyKeys.ENABLE_OTP, Boolean.class)){
            return required;
        }
        ActiveUser activeUser = (ActiveUser) auth.getPrincipal();
        if (activeUser.getApplicationType().equals(Application.B2B)) {
            required = true;
            String userIP = IPUtils.getClientIP(request);
            if(isUserCompanyIPWhitelisted(userIP, activeUser)){
                /*
                 * if user company ip is whitelisted then no need of
                 * otp
                 */
                required = false;
            }
        }
        return required;
    }

    private boolean isUserCompanyIPWhitelisted(String userIP, ActiveUser activeUser) {
        boolean whitelisted = false;
        List<UserSubscriptionMapping> userSubscriptions = userSubscriptionService
                .getUserSubscriptionMappingList(activeUser.getUserIdentifier());
        Set<Integer> companyIds = new HashSet<>();
        for (UserSubscriptionMapping userSubscription : userSubscriptions) {
            CompanySubscription subs = userSubscription.getSubscription();
            if (subs != null) {
                companyIds.add(subs.getCompanyId());
            }
        }
        if (!companyIds.isEmpty()) {
            List<CompanyIP> companyIps = companyIPDao.findByCompanyIdIn(companyIds);
            for (CompanyIP companyIP : companyIps) {
                if (companyIP.getIp().equals(userIP)) {
                    whitelisted = true;
                    break;
                }
            }
        }
        return whitelisted;
    }

    @Transactional
    public void respondWithOTP(ActiveUser activeUser) {
        int otp = generator.getRandomInt();
        UserOTP userOTP = new UserOTP();
        userOTP.setOtp(otp);
        userOTP.setUserId(activeUser.getUserIdentifier());
        userOTPDao.save(userOTP);
        //TODO send mail using notification service
//        MailBody mailBody = mailBodyGenerator.generateMailBody(
//                MailTemplateDetail.OTP,
//                new OtpMail(activeUser.getFullName(), otp, UserOTP.EXPIRES_IN_MINUTES));
//        MailDetails mailDetails = new MailDetails(mailBody).setMailTo(activeUser.getUsername()).setMailBCC(
//                PropertyReader.getRequiredPropertyAsString(PropertyKeys.MAIL_OTP_BCC));
//        mailSender.sendMailUsingAws(mailDetails);

    }

    @Transactional
    public void validate(String otp, ActiveUser activeUser, HttpServletRequest request, HttpServletResponse response) {
        Pageable pageable = new LimitOffsetPageRequest(0, 1, Direction.DESC, "id");
        List<UserOTP> userOTPs = userOTPDao.findLatestOTPByUserId(activeUser.getUserIdentifier(), pageable);
        if (userOTPs.isEmpty() || otp == null) {
            throw new BadRequestException(ResponseCodes.OTP_REQUIRED, ResponseErrorMessages.User.WRONG_OTP);
        }
        Calendar cal = Calendar.getInstance();
        if (cal.getTime().after(userOTPs.get(0).getExpiresAt())) {
            userOTPDao.delete(userOTPs);
            /*
             * in case OTP expired re-send otp
             */
            respondWithOTP(activeUser);
            throw new BadRequestException(ResponseCodes.OTP_REQUIRED, ResponseErrorMessages.User.OTP_EXPIRED);
        }
        if (otp.equals(userOTPs.get(0).getOtp().toString())) {
            SecurityContextUtils.grantUserAuthorityToActiveUser();
            clearUserOTP(activeUser);
            try {
                authSuccessHandler.onAuthenticationSuccess(request, response, SecurityContextUtils.getAuthentication());
            }
            catch (ServletException | IOException e) {
                throw new BadRequestException(ResponseCodes.OTP_REQUIRED, "OTP validation failed");
            }
        }
        else {
            throw new BadRequestException(ResponseCodes.OTP_REQUIRED, ResponseErrorMessages.User.WRONG_OTP);
        }
    }

    private void clearUserOTP(ActiveUser activeUser) {
        userOTPDao.deleteByUserId(activeUser.getUserIdentifier());
    }

    public static class OtpMail{
        private String userName;
        private Integer otp;
        private Integer validity;
        public OtpMail(String userName, Integer otp, Integer validity) {
            super();
            this.userName = userName;
            this.otp = otp;
            this.validity = validity;
        }
        public String getUserName() {
            return userName;
        }
        public Integer getOtp() {
            return otp;
        }
        public Integer getValidity() {
            return validity;
        }
    }
}
