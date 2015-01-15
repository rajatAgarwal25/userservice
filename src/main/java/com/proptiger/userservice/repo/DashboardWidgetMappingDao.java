package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proptiger.core.model.proptiger.DashboardWidgetMapping;

/**
 * @author Rajeev Pandey
 * 
 */
public interface DashboardWidgetMappingDao extends JpaRepository<DashboardWidgetMapping, Integer> {
    public DashboardWidgetMapping findByDashboardIdAndWidgetId(Integer dashboardId, Integer widgetId);

    public List<DashboardWidgetMapping> findByDashboardId(Integer dashboardId);
}
