package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proptiger.core.model.proptiger.Dashboard;
import com.proptiger.core.model.proptiger.Dashboard.DashboardType;

/**
 * Dashboard repository class to provide CRUD operations for Dashboard resource
 * 
 * @author Rajeev Pandey
 * 
 */
public interface DashboardDao extends JpaRepository<Dashboard, Integer>, DashboardCustomDao {

    public List<Dashboard> findByUserIdAndDashboardType(Integer userId,  DashboardType dashboardType);

    public Dashboard findByIdAndUserId(Integer dashboardId, Integer userId);

    public Dashboard findByNameAndUserIdAndDashboardType(String name, Integer userId, DashboardType dashboardType);


}
