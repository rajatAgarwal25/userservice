package com.proptiger.userservice.config.security.social;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUser;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.Application;
import com.proptiger.core.model.user.User;
import com.proptiger.core.service.ApplicationNameService;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.service.UserService;

/**
 * Social user details service
 * 
 * @author Rajeev Pandey
 * 
 */
public class SocialUserDetailServiceImpl implements SocialUserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(SocialUserDetailServiceImpl.class);

    @Autowired
    private UserService userService;

    @Override
    public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {
        SocialUser socialUser = null;
        User user = userService.getUserByIdWithRoles(Integer.parseInt(userId));
        if (user != null) {
            String password = user.getPassword() == null ? "" : user.getPassword();
            Application applicationType = ApplicationNameService.getApplicationTypeOfRequest();
            socialUser = new ActiveUser(
                    user.getFullName(),
                    user.getId(),
                    user.getEmail(),
                    password,
                    true,
                    true,
                    true,
                    true,
                    SecurityContextUtils.getDefaultAuthority(user),
                    applicationType);
        }
        else {
            logger.error("User not found with id {}", userId);
        }
        return socialUser;
    }

}