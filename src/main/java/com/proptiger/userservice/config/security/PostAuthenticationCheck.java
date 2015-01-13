package com.proptiger.userservice.config.security;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.ActivationStatus;
import com.proptiger.core.enums.Application;
import com.proptiger.core.enums.Status;
import com.proptiger.core.exception.AuthenticationExceptionImpl;
import com.proptiger.core.model.companyuser.CompanyUser;
import com.proptiger.userservice.service.CompanyUserService;
import com.proptiger.userservice.service.UserSubscriptionService;

/**
 * Class to handle post auth checks. After successful credential validation
 * there may be some checks say on type of user and their roles and access type.
 * 
 * @author Rajeev Pandey
 * 
 */
public class PostAuthenticationCheck implements UserDetailsChecker {
    private static Logger           logger = LoggerFactory.getLogger(PostAuthenticationCheck.class);

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    @Autowired
    private CompanyUserService      companyUserService;

    @Override
    public void check(UserDetails toCheck) {
        if (toCheck instanceof ActiveUser) {
            ActiveUser activeUser = ((ActiveUser) toCheck);
            validateSubscription(activeUser);
        }
    }

    /**
     * Validate subscription and account expiration of user
     * 
     * @param activeUser
     */
    private void validateSubscription(ActiveUser activeUser) {
        if (activeUser.getApplicationType().equals(Application.B2B)) {
            /*
             * If a b2b-user's permissions have expired then login request is
             * denied
             */
            int userId = activeUser.getUserIdentifier();
            /*
             * Throw error if user has no subscriptions at all (non-b2b user).
             */
            List<?> userSubscriptionMappingList = userSubscriptionService.getUserSubscriptionMappingList(userId);
            if (userSubscriptionMappingList == null || userSubscriptionMappingList.isEmpty()) {
                throw new AuthenticationExceptionImpl(ResponseCodes.ACCESS_DENIED, ResponseErrorMessages.User.NON_B2B_USER);
            }

            /* Throw error if user has no *active* subscriptions. */
            List<?> permissionList = userSubscriptionService.getUserAppSubscriptionDetails(userId);
            if (permissionList == null || permissionList.isEmpty()) {
                throw new AuthenticationExceptionImpl(
                        ResponseCodes.ACCESS_EXPIRED,
                        ResponseErrorMessages.User.EXPIRED_PERMISSION_B2B_USER);
            }
        }
        else if (activeUser.getApplicationType().equals(Application.RMP)) {
            /*
             * If re-sale market place application type then check if user is
             * company user or not. In case if user not a company user then
             * respond with error message.
             */
            List<CompanyUser> companyUsers = companyUserService.getCompanyUsers(activeUser.getUserIdentifier());
            if (companyUsers == null || companyUsers.isEmpty()) {
                // not a company user
                throw new AuthenticationExceptionImpl(ResponseCodes.ACCESS_DENIED, ResponseErrorMessages.User.NON_RMP_USER);
            }
            else {
                boolean isActiveUser = false;
                boolean isActiveCompany = false;
                for (CompanyUser companyUser : companyUsers) {
                    if (companyUser.getStatus().equals(ActivationStatus.Active)) {
                        isActiveUser = true;
                    }
                    if (companyUser.getCompany() != null && (companyUser.getCompany().getStatus().equals(Status.Active))) {
                        isActiveCompany = true;
                    }
                    if (isActiveCompany && isActiveUser) {
                        break;
                    }
                }
                if (!(isActiveCompany && isActiveUser)) {
                    throw new AuthenticationExceptionImpl(
                            ResponseCodes.ACCESS_DENIED,
                            ResponseErrorMessages.User.INACTIVE_RMP_USER_COMPANY);
                }
            }
        }
    }
}
