package com.proptiger.userservice.config.security;


import java.io.IOException;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.mvc.UserController;
import com.proptiger.userservice.service.OTPService;

/**
 * Auth success handler to manage session and response after authentication. It
 * put the logged in user details to request session so that would be available
 * to controllers
 * 
 * @author Rajeev Pandey
 * 
 */
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private UserController userController;

    @Autowired
    private OTPService     otpService;

    public AuthSuccessHandler() {
        super();
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication) throws ServletException, IOException {

        ActiveUser activeUser = SecurityContextUtils.putActiveUserInSession(request, authentication);
        clearAuthenticationAttributes(request);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(
                "fieldFilter",
                SimpleBeanPropertyFilter.serializeAllExcept(new HashSet<String>()));

        ObjectMapper mapper = userController.getMapper();
        response.getWriter().print(
                mapper.writer(filterProvider).writeValueAsString(
                        userController.getUserDetails(activeUser)));
    }
}
