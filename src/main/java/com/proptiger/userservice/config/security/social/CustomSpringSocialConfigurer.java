package com.proptiger.userservice.config.security.social;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialAuthenticationProvider;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.SpringSocialConfigurer;

import com.proptiger.core.util.Constants;
import com.proptiger.core.util.PropertyReader;
import com.proptiger.userservice.config.security.AuthSuccessHandler;

/**
 * Spring social filter configuration that will intercept provider login
 * starting
 * 
 * @author Rajeev Pandey
 *
 */
public class CustomSpringSocialConfigurer extends SpringSocialConfigurer {

    @Autowired
    private AuthSuccessHandler authSuccessHandler;

    @Autowired
    private PropertyReader     propertyReader;
    

    public CustomSpringSocialConfigurer() {
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        UsersConnectionRepository usersConnectionRepository = getDependency(
                applicationContext,
                UsersConnectionRepository.class);
        SocialAuthenticationServiceLocator authServiceLocator = getDependency(
                applicationContext,
                SocialAuthenticationServiceLocator.class);
        SocialUserDetailsService socialUsersDetailsService = getDependency(
                applicationContext,
                SocialUserDetailsService.class);

        CustomSocialAuthFilter filter = new CustomSocialAuthFilter(
                propertyReader,
                http.getSharedObject(AuthenticationManager.class),
                new AuthenticationNameUserIdSource(),
                usersConnectionRepository,
                authServiceLocator);
        /*
         * Since we are not using default table provided by Spring to disabling
         * this. We are saving part of connection data in FORUM_USER table,
         * could introduce more fields in same table
         */
        filter.setUpdateConnections(false);
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(Constants.Security.LOGIN_URL, "POST"));
        filter.setFilterProcessesUrl(Constants.Security.LOGIN_URL);
        filter.setAuthenticationFailureHandler(new SocialAuthFailureHandler("/"+Constants.Security.REGISTER_URL));
        
        /*
         * set authentication success handler to return same result as of
         * app/v1/login
         */
        filter.setAuthenticationSuccessHandler(authSuccessHandler);
        filter.setSignupUrl(Constants.Security.REGISTER_URL);
        RememberMeServices rememberMe = http.getSharedObject(RememberMeServices.class);
        if (rememberMe != null) {
            filter.setRememberMeServices(rememberMe);
        }
        http.authenticationProvider(
                new SocialAuthenticationProvider(usersConnectionRepository, socialUsersDetailsService))
                .addFilterBefore(postProcess(filter), AbstractPreAuthenticatedProcessingFilter.class);
    }

    private <T> T getDependency(ApplicationContext applicationContext, Class<T> dependencyType) {
        try {
            T dependency = applicationContext.getBean(dependencyType);
            return dependency;
        }
        catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("SpringSocialConfigurer depends on " + dependencyType.getName()
                    + ". No single bean of that type found in application context.", e);
        }
    }
}
