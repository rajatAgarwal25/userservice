package com.proptiger.userservice.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.Application;
import com.proptiger.core.enums.MailTemplateDetail;
import com.proptiger.core.enums.notification.MediumType;
import com.proptiger.core.enums.notification.NotificationTypeEnum;
import com.proptiger.core.enums.notification.Tokens;
import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.internal.dto.mail.DefaultMediumDetails;
import com.proptiger.core.internal.dto.mail.MailBody;
import com.proptiger.core.internal.dto.mail.MailDetails;
import com.proptiger.core.internal.dto.mail.MediumDetails;
import com.proptiger.core.model.CompanyIP;
import com.proptiger.core.model.notification.external.NotificationCreatorServiceRequest;
import com.proptiger.core.model.proptiger.CompanySubscription;
import com.proptiger.core.model.proptiger.UserSubscriptionMapping;
import com.proptiger.core.model.user.UserAttribute;
import com.proptiger.core.pojo.LimitOffsetPageRequest;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.repo.APIAccessLogDao;
import com.proptiger.core.service.mail.MailSender;
import com.proptiger.core.service.mail.TemplateToHtmlGenerator;
import com.proptiger.core.util.Constants;
import com.proptiger.core.util.HttpRequestUtil;
import com.proptiger.core.util.IPUtils;
import com.proptiger.core.util.PropertyKeys;
import com.proptiger.core.util.PropertyReader;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.config.security.AuthSuccessHandler;
import com.proptiger.userservice.model.UserOTP;
import com.proptiger.userservice.repo.CompanyIPDao;
import com.proptiger.userservice.repo.UserAttributeDao;
import com.proptiger.userservice.repo.UserOTPDao;
import com.proptiger.userservice.util.OTPGenerator;

/**
 * Service class to handle generation/validation of one time password.
 * 
 * @author Rajeev Pandey
 *
 */
public class OTPService {

    private static final String     OTP_RESPONSE_MESSAGE = "New OTP has been sent over to your registered Email and Mobile Phone";

    @Autowired
    private APIAccessLogDao         accessLogDao;

    @Autowired
    private UserOTPDao              userOTPDao;

    private OTPGenerator            generator            = new OTPGenerator();

    @Autowired
    private AuthSuccessHandler      authSuccessHandler;

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    @Autowired
    private CompanyIPDao            companyIPDao;

    @Autowired
    private UserAttributeDao        userAttributeDao;

    @Autowired
    private HttpRequestUtil         httpRequestUtil;

    @Autowired
    private MailSender              mailSender;

    @Autowired
    private TemplateToHtmlGenerator htmlGenerator;

    public boolean isOTPRequired(Authentication auth, HttpServletRequest request) {
        boolean required = false;
        if (!PropertyReader.getRequiredPropertyAsType(PropertyKeys.ENABLE_OTP, Boolean.class)) {
            return required;
        }
        ActiveUser activeUser = (ActiveUser) auth.getPrincipal();

        if (activeUser.getApplicationType().equals(Application.B2B)) {
            required = true;
            UserAttribute userAttribute = userAttributeDao.findByUserIdAndAttributeNameAndAttributeValue(
                    activeUser.getUserIdentifier(),
                    Constants.User.OTP_ATTRIBUTE_NAME,
                    Constants.User.OTP_ATTRIBUTE_VALUE_TRUE);
            if (userAttribute != null) {
                required = false;
            }
            else {
                String userIP = IPUtils.getClientIP(request);
                if (isUserCompanyIPWhitelisted(userIP, activeUser)) {
                    /*
                     * if user company ip is whitelisted then no need of otp
                     */
                    required = false;
                }
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

    private String getAPIUrl() {

        return PropertyReader.getRequiredPropertyAsString(PropertyKeys.PROPTIGER_URL) + PropertyReader
                .getRequiredPropertyAsString(PropertyKeys.NOTIFICATION_SEND_URL);
    }

    @Transactional
    public String respondWithOTP(ActiveUser activeUser) {
        int otp = generator.getRandomInt();

        sendOTPOverMail(otp, activeUser);
        sendOTPOverSms(otp, activeUser);
        return OTP_RESPONSE_MESSAGE;
    }

    private void sendOTPOverSms(int otp, ActiveUser activeUser) {
        URI uri = URI.create(UriComponentsBuilder.fromUriString(getAPIUrl()).build().encode().toString());

        List<MediumDetails> mediumDetails = new ArrayList<MediumDetails>();
        mediumDetails.add(new DefaultMediumDetails(MediumType.Sms));

        Map<String, Object> notificationPayloadMap = new HashMap<String, Object>();
        notificationPayloadMap.put(Tokens.LoginOtp.UserName.name(), activeUser.getFullName());
        notificationPayloadMap.put(Tokens.LoginOtp.OtpCode.name(), otp);

        NotificationCreatorServiceRequest request = new NotificationCreatorServiceRequest(
                NotificationTypeEnum.LoginOtp,
                activeUser.getUserIdentifier(),
                notificationPayloadMap,
                mediumDetails);

        httpRequestUtil.postAndReturnInternalJsonRequest(uri, request, APIResponse.class);
    }

    private void sendOTPOverMail(int otp, ActiveUser activeUser) {
        UserOTP userOTP = new UserOTP();
        userOTP.setOtp(otp);
        userOTP.setUserId(activeUser.getUserIdentifier());
        userOTPDao.save(userOTP);
        MailBody mailBody = htmlGenerator.generateMailBody(MailTemplateDetail.OTP, new OtpMail(
                activeUser.getFullName(),
                otp,
                UserOTP.EXPIRES_IN_MINUTES));
        MailDetails mailDetails = new MailDetails(mailBody).setMailTo(activeUser.getUsername()).setMailBCC(
                PropertyReader.getRequiredPropertyAsString(PropertyKeys.MAIL_OTP_BCC));
        mailSender.sendMailUsingAws(mailDetails);

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

    public static class OtpMail {
        private String  userName;
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
