package org.springframework.session.data.redis;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.data.redis.cache.CustomRedisCacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.session.MapSession;
import org.springframework.session.data.redis.RedisOperationsSessionRepository.RedisSession;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.model.user.User;
import com.proptiger.core.util.Constants;
import com.proptiger.core.util.PropertyKeys;
import com.proptiger.core.util.PropertyReader;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.model.UserSession;
import com.proptiger.userservice.repo.UserSessionDao;
import com.proptiger.userservice.service.UserService;

/**
 * @author Rajeev Pandey
 * 
 */
public class DatabaseSessionOperations {
    private static Logger           logger                          = LoggerFactory
                                                                            .getLogger(DatabaseSessionOperations.class);

    public static final String      SPRING_SECURITY_CONTEXT         = "SPRING_SECURITY_CONTEXT";
    private static final String     DB_PERSISTED_SESSION_KEY_PREFIS = "db:persisted:sessions:";

    private int                     maxInactiveInterval;

    @Autowired
    private UserSessionDao          userSessionDao;

    @Autowired
    private UserService             userService;

    private CustomRedisCacheManager cacheManager;

    public DatabaseSessionOperations(CustomRedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void init() {
        maxInactiveInterval = PropertyReader.getRequiredPropertyAsType(
                PropertyKeys.SESSION_MAX_INTERACTIVE_INTERVAL,
                Integer.class);
    }

    @Async
    public void save(RedisSession session) {
        Object sessionAttribute = session.getAttribute(Constants.LOGIN_INFO_OBJECT_NAME);
        if (sessionAttribute != null && sessionAttribute instanceof ActiveUser) {
            ActiveUser activeUser = (ActiveUser) sessionAttribute;
            if (!isSessionPersistedInDB(session.getId())) {
                UserSession userSession = new UserSession();
                userSession.setSessionId(session.getId());
                userSession.setUserId(activeUser.getUserIdentifier());
                userSession.setCreationTime(new Date(session.getCreationTime()));
                userSession.setLastAccessedTime(new Date(session.getLastAccessedTime()));
                try {
                    logger.debug("saving session id {} in database", session.getId());
                    userSessionDao.save(userSession);
                    putSessionIdInRedis(session.getId(), true);
                }
                catch (Exception e) {
                    logger.error("Error while persisting user session for session id {}", session.getId(), e);
                    putSessionIdInRedis(session.getId(), true);
                }
            }
            else{
                //update session last accessed time in database
                UserSession userSession = userSessionDao.getUserSessionBySessionId(session.getId());
                if(userSession != null){
                    Date lastAccessedTime = new Date(session.getLastAccessedTime());
                    if(lastAccessedTime.after(userSession.getLastAccessedTime())){
                        userSession.setUserId(activeUser.getUserIdentifier());
                        userSession.setLastAccessedTime(lastAccessedTime);
                        userSessionDao.save(userSession);
                    }
                   
                }
               
            }
        }
    }

    private void putSessionIdInRedis(String id, boolean value) {
        Cache cache = cacheManager.getCache(DB_PERSISTED_SESSION_KEY_PREFIS, maxInactiveInterval);
        cache.put(id, value);
    }

    private boolean isSessionPersistedInDB(String id) {
        Cache cache = cacheManager.getCache(DB_PERSISTED_SESSION_KEY_PREFIS, maxInactiveInterval);
        ValueWrapper val = cache.get(id);
        if (val != null) {
            return (boolean) val.get();
        }
        return false;
    }

    private boolean isSessionIdProbablyInDB(String sessionId) {
        boolean sessionInDB = false;
        Cache cache = cacheManager.getCache(DB_PERSISTED_SESSION_KEY_PREFIS, maxInactiveInterval);
        ValueWrapper val = cache.get(sessionId);
        if (val != null) {
            sessionInDB = (boolean) val.get();
        }
        else {
            // session may be in db if no hint in redis
            sessionInDB = true;
        }

        return sessionInDB;

    }

    private void removeSessionIdFromRedis(String sessionId) {
        Cache cache = cacheManager.getCache(DB_PERSISTED_SESSION_KEY_PREFIS, maxInactiveInterval);
        cache.evict(sessionId);
    }

    public RedisSession getSession(
            RedisAndDBOperationsSessionRepository redisAndDBOperationsSessionRepository,
            String id) {
        if (isSessionIdProbablyInDB(id)) {
            UserSession userSession = userSessionDao.getUserSessionBySessionId(id);
            if (userSession != null) {
                logger.debug("found session id {} in database", id);
                MapSession loaded = new MapSession();
                loaded.setId(id);
                loaded.setCreationTime(userSession.getCreationTime().getTime());
                loaded.setLastAccessedTime(userSession.getLastAccessedTime().getTime());
                loaded.setMaxInactiveInterval(maxInactiveInterval);
                User user = userService.getUserByIdWithRoles(userSession.getUserId());
                Authentication auth = SecurityContextUtils.createNewAuthentication(user);
                ActiveUser principle = (ActiveUser) auth.getPrincipal();
                loaded.setAttribute(Constants.LOGIN_INFO_OBJECT_NAME, principle);
                SecurityContext securityContext = new SecurityContextImpl();
                securityContext.setAuthentication(auth);
                loaded.setAttribute(SPRING_SECURITY_CONTEXT, securityContext);
                RedisSession result = redisAndDBOperationsSessionRepository.new RedisSession(loaded);
                putSessionIdInRedis(id, true);
                return result;
            }
            else {
                /*
                 * put a hint in redis that this session id is not in database
                 * so that select query will not run again*
                 */
                putSessionIdInRedis(id, false);
            }
        }
        return null;
    }

    @Async
    public void delete(String id) {
        logger.debug("delete session id {}",id);
        userSessionDao.deleteBySessionId(id);
        removeSessionIdFromRedis(id);
    }

}
