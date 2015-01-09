package com.proptiger.userservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.userservice.model.UserSession;

/**
 * @author Rajeev Pandey
 *
 */
public interface UserSessionDao extends JpaRepository<UserSession, Integer>{

    public UserSession getUserSessionBySessionId(String sessionId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession WHERE sessionId = ?1")
    void deleteBySessionId(String sessionId);
}
