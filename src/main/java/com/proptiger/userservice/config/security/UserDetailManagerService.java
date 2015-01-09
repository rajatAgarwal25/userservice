package com.proptiger.userservice.config.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.Application;
import com.proptiger.core.model.user.User;
import com.proptiger.core.service.ApplicationNameService;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.repo.UserDao;

/**
 * Custom implementation of UserDetailsService to provide criteria to
 * authenticate a user. This class uses database to authenticate.
 * 
 * @author Rajeev Pandey
 * @author azi
 * 
 */
@Service
public class UserDetailManagerService implements UserDetailsService {

    private static Logger           logger   = LoggerFactory.getLogger(UserDetailManagerService.class);

    @Autowired
    private UserDao                 userDao;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ActiveUser userDetails = null;
        User user = null;
        if (username != null && !username.isEmpty()) {
            try {
                user = userDao.findByEmailWithRoles(username);
                if (user != null) {
                    Application applicationType = ApplicationNameService.getApplicationTypeOfRequest();
                    String password = user.getPassword() == null ? "" : user.getPassword();
                    userDetails = new ActiveUser(
                            user.getFullName(),
                            user.getId(),
                            user.getEmail(),
                            password,
                            true,
                            true,
                            true,
                            true,
                           SecurityContextUtils.getDefaultAuthority(user), applicationType);
                }
                else {
                    logger.error("User not found with email {}", username);
                }
            }
            catch (Exception e) {
                logger.error("error while fetching user ",e);
            }
        }
        // if no user found with given username(email)
        if (userDetails == null) {
            throw new UsernameNotFoundException("User name or password are incorrect");
        }
        return userDetails;
    }

}
