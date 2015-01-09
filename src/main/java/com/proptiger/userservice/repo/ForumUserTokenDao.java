package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proptiger.core.model.user.ForumUserToken;

/**
 * @author Rajeev Pandey
 *
 */
public interface ForumUserTokenDao extends JpaRepository<ForumUserToken, Integer>{
    
    public ForumUserToken findByToken(String token);
    
    @Query("select FUT from ForumUserToken FUT where FUT.userId=?1 order by FUT.tokenId desc")
    public List<ForumUserToken> findLatestTokenByUserId(int userId, Pageable limitOffsetPageRequest);
    
}
