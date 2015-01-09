package com.proptiger.userservice.config.security;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import com.proptiger.core.util.SecurityContextUtils;

/**
 * Custom implementation of remember me filter to set active user data in
 * session in a flow when user gets auto logged in based on remember me cookie,
 * and continue in filter chain so no need of redirection required in
 * authentication success handler.
 * 
 * This class handles the user who logged in using rememberme on website when
 * login was based on memcache and since they should not be logged out becoz we
 * removed dependency over memcache we are making user auto login in api if
 * PHPSESSID is found in memcache else normal flow should work.
 * 
 * @author Rajeev Pandey
 *
 */
public class CustomRememberMeAuthFilter extends RememberMeAuthenticationFilter {

    private SessionAuthenticationStrategy sessionStrategy = new NullAuthenticatedSessionStrategy();

    public CustomRememberMeAuthFilter(AuthenticationManager authenticationManager, RememberMeServices rememberMeServices) {
        super(authenticationManager, rememberMeServices);
    }

    /*
     * Putting Active user details in session that would enable controllers to
     * access the same. So no need to redirect to the url asked just let it pass
     * through filter chain.
     */
    @Override
    protected void onSuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authResult) {
        SecurityContextUtils.putActiveUserInSession(request, authResult);
        sessionStrategy.onAuthentication(authResult, request, response);
    }

    public SessionAuthenticationStrategy getSessionStrategy() {
        return sessionStrategy;
    }

    public void setSessionStrategy(SessionAuthenticationStrategy sessionStrategy) {
        this.sessionStrategy = sessionStrategy;
    }
    
    
}
