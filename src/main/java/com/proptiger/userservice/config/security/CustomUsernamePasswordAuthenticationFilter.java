package com.proptiger.userservice.config.security;


import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.exception.AuthenticationExceptionImpl;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.service.OTPService;

/**
 * 
 * @author Rajeev Pandey
 *
 */
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private OTPService otpService;

    public CustomUsernamePasswordAuthenticationFilter(OTPService service) {
        this.otpService = service;
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        boolean otpRequired = otpService.isOTPRequired(authResult, request);
        if(otpRequired){
            /*
             * grant PRE_AUTH_USER role to all users who requires otp
             */
            authResult = SecurityContextUtils.grantPreAuthAuthority(authResult);
        }
        
        /*
         * let Authentication set in security context even if auth did not
         * complete yet, as this will be handled in filters
         */
        SecurityContextUtils.setAuthentication(authResult);
        // Fire event
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }
        if (otpRequired) {
            /*
             * this need to be done as success handler will be skipped for OTP flow
             */
            SecurityContextUtils.putActiveUserInSession(request, authResult);
            /*
             * user authentication is not complete yet, send him a OTP over
             * specified channels and validate the same before marking that user
             * fully authenticated
             */
            otpService.respondWithOTP((ActiveUser) authResult.getPrincipal());
            getFailureHandler().onAuthenticationFailure(
                    request,
                    response,
                    new AuthenticationExceptionImpl(ResponseCodes.OTP_REQUIRED, ResponseErrorMessages.User.OTP_REQUIRED));
            return;
        }
        getRememberMeServices().loginSuccess(request, response, authResult);
        getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }
}
