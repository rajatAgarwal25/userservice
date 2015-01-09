package com.proptiger.userservice.config.security;


import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.userservice.model.UserPersistentLogin;
import com.proptiger.userservice.repo.UserPersistentLoginDao;

/**
 * JDBC based persistent login token repository implementation.
 * 
 * @author Rajeev Pandey
 * 
 */
public class CustomJdbcPersistentTokenRepository implements PersistentTokenRepository {

    private static Logger          logger = LoggerFactory.getLogger(CustomJdbcPersistentTokenRepository.class);
    @Autowired
    private UserPersistentLoginDao persistentLoginDao;

    @Override
    @Async
    @Transactional
    public void createNewToken(PersistentRememberMeToken token) {
        logger.debug("saving persistence token series {} for {}", token.getSeries(), token.getUsername());
        UserPersistentLogin persistentLogin = new UserPersistentLogin();
        persistentLogin.setSeries(token.getSeries());
        persistentLogin.setToken(token.getTokenValue());
        persistentLogin.setUserName(token.getUsername());
        persistentLoginDao.save(persistentLogin);
    }

    @Override
    @Async
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        UserPersistentLogin token = persistentLoginDao.findBySeries(series);
        if (token != null) {
            token.setToken(tokenValue);
            persistentLoginDao.save(token);
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            UserPersistentLogin userPersistentLogin = persistentLoginDao.findBySeries(seriesId);
            if(userPersistentLogin != null){
                return userPersistentLogin.toPersistentRememberMeToken();
            }
        }
        catch (Exception e) {
            logger.error("Error while fetching series {}", seriesId, e);
        }
        return null;
    }

    @Override
    public void removeUserTokens(String userName) {
        /*
         * delete all persistent login tokens by username. This should be called
         * in case of cookie theft scenario and not in logout flow
         */
        persistentLoginDao.deleteByUserName(userName);
    }

}
