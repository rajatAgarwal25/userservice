package com.proptiger.userservice.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.core.model.user.UserContactNumber;

/**
 * 
 * @author azi
 * 
 */

public interface UserContactNumberDao extends JpaRepository<UserContactNumber, Integer> {
    public List<UserContactNumber> findByContactNumber(String contactNumber);

    @Modifying
    @Transactional
    @Query("UPDATE UserContactNumber SET priority=priority+1 WHERE userId = ?1")
    public int incrementPriorityForUser(int userId);

    @Query("select UCN from UserContactNumber UCN where UCN.userId in (?1)")
    public List<UserContactNumber> getContactNumbersByUserId(Set<Integer> clientIds);
    
    public List<UserContactNumber> findByUserIdOrderByPriorityAsc(int userId);
}
