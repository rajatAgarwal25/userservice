package org.springframework.session.data.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.session.ExpiringSession;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.util.Constants;

/**
 * A session repository to create/save/delete/get session in redis using
 * RedisOperationsSessionRepository as well as in database, as a fallback in
 * case of redis fail or flush.
 * 
 * @author Rajeev Pandey
 * 
 */
public class RedisAndDBOperationsSessionRepository extends RedisOperationsSessionRepository {

    @Autowired
    private DatabaseSessionOperations databaseSessionOperations;

    public RedisAndDBOperationsSessionRepository(
            RedisOperations<String, ExpiringSession> sessionRedisOperations,
            RedisOperations<String, String> expirationRedisOperations,
            DatabaseSessionOperations databaseSessionOperations) {
        super(sessionRedisOperations, expirationRedisOperations);
        this.databaseSessionOperations = databaseSessionOperations;
    }

    @Override
    public RedisSession createSession() {
        return super.createSession();
    }

    @Override
    public void save(RedisSession session) {
        databaseSessionOperations.save(session);
        super.save(session);
    }

    @Override
    public void delete(String sessionId) {
        super.delete(sessionId);
        databaseSessionOperations.delete(sessionId);
    }

    @Override
    public RedisSession getSession(String id) {
        RedisSession result = super.getSession(id);
        if (result == null) {
            // find in database as jsessionid received in request, so redis
            // might have flushed
            result = databaseSessionOperations.getSession(this, id);
        }
        else if (result.getAttribute(DatabaseSessionOperations.SPRING_SECURITY_CONTEXT) == null) {
            /*
             * Due to a bug in RedisSessionExpirationPolicy, it deletes spring
             * security context. Should be removed once resolved
             */
            SecurityContext securityContext = new SecurityContextImpl();
            Object principle = result.getAttribute(Constants.LOGIN_INFO_OBJECT_NAME);
            if (principle != null && principle instanceof ActiveUser) {
                ActiveUser activeUser = (ActiveUser) principle;
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        activeUser,
                        null,
                        activeUser.getAuthorities());
                securityContext.setAuthentication(auth);
                result.setAttribute(DatabaseSessionOperations.SPRING_SECURITY_CONTEXT, securityContext);
            }

        }
        return result;
    }

}
