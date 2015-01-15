package com.proptiger.userservice.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.data.redis.RedisAndDBOperationsSessionRepository;
import org.springframework.stereotype.Service;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.util.Constants;
import com.proptiger.core.util.RequestHolderUtil;

/**
 * @author Rajeev Pandey
 *
 */
@Service
public class UserSessionService {
    
    @Autowired
    private RedisAndDBOperationsSessionRepository redisAndDBOperationsSessionRepository;

    public ActiveUser getActiveSessionOfRequest(HttpServletRequest request){
        String jsessionId = RequestHolderUtil.getJsessionIdFromRequestCookie();
        if(jsessionId != null && !jsessionId.isEmpty()){
            ActiveUser activeUser =  redisAndDBOperationsSessionRepository.getActiveUserFromSession(jsessionId);
            return activeUser;
        }
        else{
            
        }
        return null;
    }
}
