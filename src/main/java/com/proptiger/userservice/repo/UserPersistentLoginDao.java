package com.proptiger.userservice.repo;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.userservice.model.UserPersistentLogin;

/**
 * @author Rajeev Pandey
 *
 */
public interface UserPersistentLoginDao extends JpaRepository<UserPersistentLogin, Integer>{
    
    @Modifying
    @Transactional
    @Query("Update UserPersistentLogin UPL set token=?2, lastUsed=?2 WHERE series = ?1")
    public void updateToken(String series, String tokenValue, Date lastUsed);

    public UserPersistentLogin findBySeries(String seriesId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserPersistentLogin WHERE userName = ?1 and series=?2")
    public void deleteByUserNameAndSeries(String userName, String series);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserPersistentLogin WHERE userName = ?1")
    public void deleteByUserName(String userName);

}
