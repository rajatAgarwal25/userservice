package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proptiger.core.model.user.MasterRoles;

/**
 * @author Rajeev Pandey
 *
 */
public interface MasterRolesDao extends JpaRepository<MasterRoles, Integer> {

    public List<MasterRoles> findMasterRolesByNameIn(List<String> roleNames);
}
