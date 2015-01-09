package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.core.model.user.UserRoles;

public interface UserRolesDao extends JpaRepository<UserRoles, Integer>{

    @Query("select MR.name from UserRoles UR join UR.role MR where UR.userId=?1")
    public List<String> getUserRolesName(int userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRoles UR WHERE UR.userId = ?1 and UR.roleId in ?2 ")
    void deleteRolesOfUserId(Integer userId, List<Integer> roleIds);
}
