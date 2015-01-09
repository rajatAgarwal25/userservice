package com.proptiger.userservice.config.security;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.RedirectStrategy;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.handler.ResponseErrorWriter;
import com.proptiger.core.util.IPUtils;

/**
 * This handler overrides the dfault implementation not to redirect on any url
 * rather display a session expiry message to user
 * 
 * @author Rajeev Pandey
 *
 */
public class SessionExpiryStrategyHandler implements RedirectStrategy {

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String userIpAddress = IPUtils.getClientIP(request);
        ResponseErrorWriter.writeErrorToResponse(
                response,
                ResponseCodes.SESSION_EXPIRED,
                ResponseErrorMessages.User.SESSION_EXPIRED_DUPLICATE_LOGIN,
                userIpAddress);
    }

}
