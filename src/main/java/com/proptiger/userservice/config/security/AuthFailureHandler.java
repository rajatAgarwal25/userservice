package com.proptiger.userservice.config.security;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.exception.APIException;
import com.proptiger.core.handler.ResponseErrorWriter;
import com.proptiger.core.util.IPUtils;

/**
 * Handle authenication failure case.
 * @author Rajeev Pandey
 *
 */
public class AuthFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String code = ResponseCodes.BAD_CREDENTIAL;
        if(exception instanceof APIException){
            code = ((APIException)exception).getResponseCode();
        }
        String userIpAddress = IPUtils.getClientIP(request);
        ResponseErrorWriter.writeErrorToResponse(
                response,
                code,
                exception.getMessage() != null ? exception.getMessage() : ResponseErrorMessages.User.BAD_CREDENTIAL,
                userIpAddress);
    }
}
