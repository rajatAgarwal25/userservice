package com.proptiger.userservice.config.security;


import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;

import com.proptiger.core.enums.security.UserRole;
import com.proptiger.core.util.Constants;
import com.proptiger.core.util.SecurityContextUtils;

/**
 * Switch User processing filter responsible for user context switching. This
 * filter is similar to Unix 'su' however for Spring Security-managed web
 * applications. A common use-case for this feature is the ability to allow
 * higher-authority users (e.g. ADMIN) to switch to a regular user (e.g. USER).
 * In case user does not enough authority to call switch or exit url, throw
 * access denied, for ex if normal user call exit/switch url, or even admin call
 * exit url, without switch to a normal user.
 * 
 * @author Rajeev Pandey
 *
 */
public class CustomSwitchUserFilter extends SwitchUserFilter {


    @Override
    protected Authentication attemptExitUser(HttpServletRequest request)
            throws AuthenticationCredentialsNotFoundException {
        if (canAttempExitUser(request)) {
            return super.attemptExitUser(request);
        }
        else {
            throw new AccessDeniedException("");
        }
    }

    private boolean canAttempExitUser(HttpServletRequest request) {
        Authentication current = SecurityContextUtils.getAuthentication();
        if (null == current) {
            throw new AuthenticationCredentialsNotFoundException(messages.getMessage(
                    "SwitchUserFilter.noCurrentUser",
                    "No current user associated with this request"));
        }
        Collection<? extends GrantedAuthority> authorities = current.getAuthorities();
        boolean attempt = false;
        for (GrantedAuthority auth : authorities) {
            //admin call flow to check exit user call first before switch user call
            if (auth.getAuthority().equals(UserRole.Admin.name()) && isSwitchUserCall(request)) {
                attempt = true;
                break;
            }
            // check for switch user type of authority
            if (auth instanceof SwitchUserGrantedAuthority) {
                attempt = true;
                break;
            }
        }
        return attempt;
    }

    private boolean isSwitchUserCall(HttpServletRequest request) {
        return request.getRequestURI().contains(Constants.Security.SUDO_USER_URL);
    }

}
