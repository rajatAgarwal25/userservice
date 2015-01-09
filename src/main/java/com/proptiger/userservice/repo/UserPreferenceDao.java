package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.proptiger.core.enums.Application;
import com.proptiger.core.model.user.UserPreference;

/**
 * 
 * @author Azitabh Ajit
 * 
 */

public interface UserPreferenceDao extends PagingAndSortingRepository<UserPreference, Integer> {
    public List<UserPreference> findByuserId(int userId);

    public UserPreference findByUserIdAndApp(int userId, Application app);
}
