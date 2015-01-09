package com.proptiger.userservice.config.security;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.proptiger.userservice.repo.UserPersistentLoginDao;

/**
 * Persistence token based
 * 
 * @author Rajeev Pandey
 * 
 */
public class CustomPersistentTokenBasedRememberMeServices extends PersistentTokenBasedRememberMeServices {

    private static Logger                       logger = LoggerFactory
                                                               .getLogger(CustomPersistentTokenBasedRememberMeServices.class);
    public static String SEPERATOR = "$$";
    
    @Autowired
    private UserPersistentLoginDao persistentLoginDao;
    
    public CustomPersistentTokenBasedRememberMeServices(
            String key,
            UserDetailsService userDetailsService,
            PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String rememberMeCookie = extractRememberMeCookie(request);
        if (rememberMeCookie == null || rememberMeCookie.length() == 0) {
            logger.debug("rememberme Cookie was empty");
            cancelCookie(request, response);
            return;
        }
        String[] cookieTokens = decodeCookie(rememberMeCookie);

        if (authentication != null) {
            /*
             * delete token on the basis of series and username instead of only
             * username as this is default implementations in spring. So other
             * sessions with same username should persist if one session logout
             */
            logger.debug("delete rememberme token series {} for {}", cookieTokens[0], authentication.getName());
            persistentLoginDao.deleteByUserNameAndSeries(authentication.getName(), cookieTokens[0]);
        }
    }

}
