package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.userservice.model.UserOTP;

/**
 * @author Rajeev Pandey
 *
 */
public interface UserOTPDao extends JpaRepository<UserOTP, Integer> {

    @Query("select UO from UserOTP UO where userId = ?1")
    List<UserOTP> findLatestOTPByUserId(Integer userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserOTP WHERE userId = ?1")
    void deleteByUserId(Integer userId);
}
