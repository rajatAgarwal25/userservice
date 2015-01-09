package com.proptiger.userservice.config.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.Application;

/**
 * This class decides the no of session creation for a user depending on for
 * which application user belongs to.
 * 
 * In future this should be handled using role assigned to a user rather than
 * relying on request header.
 * 
 * @author Rajeev Pandey
 *
 */
public class CustomConcurrentSessionControlAuthenticationStrategy extends
        ConcurrentSessionControlAuthenticationStrategy {

    private static int CONCURRENT_SESSION_ALLOWED_FOR_B2B_USER = 1;

    public CustomConcurrentSessionControlAuthenticationStrategy(SessionRegistry sessionRegistry) {
        super(sessionRegistry);
    }

    @Override
    protected int getMaximumSessionsForThisUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof ActiveUser) {
            if (((ActiveUser) authentication.getPrincipal()).getApplicationType().equals(Application.B2B)) {
                return CONCURRENT_SESSION_ALLOWED_FOR_B2B_USER;
            }
        }
        return super.getMaximumSessionsForThisUser(authentication);
    }
}
