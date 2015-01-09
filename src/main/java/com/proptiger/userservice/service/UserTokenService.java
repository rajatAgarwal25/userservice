package com.proptiger.userservice.service;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.model.user.ForumUserToken;
import com.proptiger.core.pojo.LimitOffsetPageRequest;
import com.proptiger.userservice.repo.ForumUserTokenDao;
import com.proptiger.userservice.util.PasswordUtils;

/**
 * @author Rajeev Pandey
 *
 */
@Service
public class UserTokenService {
    @Autowired
    private ForumUserTokenDao                forumUserTokenDao;

    
    public ForumUserToken createTokenForUser(Integer userId) {
        List<ForumUserToken> existingTokenList = forumUserTokenDao.findLatestTokenByUserId(userId, new LimitOffsetPageRequest(
                0,
                1));
        // token valid for 1 month
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        if (existingTokenList.isEmpty()) {
            String token = PasswordUtils.generateTokenBase64Encoded();
            token = PasswordUtils.encode(token);
            ForumUserToken forumUserToken = new ForumUserToken();
            forumUserToken.setUserId(userId);
            forumUserToken.setToken(token);
            forumUserToken.setExpirationDate(calendar.getTime());
            forumUserToken = forumUserTokenDao.save(forumUserToken);
            return forumUserToken;
        }
        ForumUserToken existingToken = existingTokenList.get(0);
        existingToken.setExpirationDate(calendar.getTime());
        existingToken = forumUserTokenDao.save(existingToken);
        return existingToken;
    }
    
    private void validateForumUserToken(ForumUserToken userToken) {
        if (userToken == null) {
            throw new BadRequestException("Invalid token");
        }
        Calendar cal = Calendar.getInstance();
        if (userToken.getExpirationDate().before(cal.getTime())) {
            throw new BadRequestException("Token expired");
        }
    }
    
    public ForumUserToken getTokenDetailsAfterValidation(String token){
        ForumUserToken userToken = forumUserTokenDao.findByToken(token);
        validateForumUserToken(userToken);
        return userToken;
    }
    
    @Async
    public void deleteTokenAsync(ForumUserToken token){
        forumUserTokenDao.delete(token);
    }
}
