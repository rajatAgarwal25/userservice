package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.proptiger.core.model.user.UserEmail;

/**
 * 
 * @author azi
 * 
 */

public interface UserEmailDao extends PagingAndSortingRepository<UserEmail, Integer> {    
    public List<UserEmail> findByEmail(String email);
}
