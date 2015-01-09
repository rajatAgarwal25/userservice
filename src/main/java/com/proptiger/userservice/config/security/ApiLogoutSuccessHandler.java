package com.proptiger.userservice.config.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * Handle logout, in case of succuessful logout request will come here
 * @author Rajeev Pandey
 *
 */
public class ApiLogoutSuccessHandler implements LogoutSuccessHandler {
    private static Logger             logger        = LoggerFactory.getLogger(ApiLogoutSuccessHandler.class);
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if(authentication != null){
            logger.debug("User {} logged out",authentication.getPrincipal());    
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}