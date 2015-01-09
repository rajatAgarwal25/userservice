package com.proptiger.userservice.config.security;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Initialize spring security system
 * 
 * @author Rajeev Pandey
 *
 */
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {

    public SecurityWebApplicationInitializer() {
        super(AppSecurityConfig.class);
    }
}