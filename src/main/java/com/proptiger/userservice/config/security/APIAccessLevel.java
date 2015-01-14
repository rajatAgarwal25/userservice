package com.proptiger.userservice.config.security;

/**
 * Currently working as a marker annotation to mark a API access level for
 * readability purpose, but in future will define access level in real.
 * 
 * @author Rajeev Pandey
 *
 */
public @interface APIAccessLevel {
    public AccessLevel[] level();
}
