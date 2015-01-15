package com.proptiger.userservice.repo;

import java.util.List;

import com.proptiger.core.model.proptiger.Dashboard;
import com.proptiger.core.pojo.FIQLSelector;

/**
 * @author Rajeev Pandey
 *
 */
public interface DashboardCustomDao {
    
    public List<Dashboard> getDashboards(FIQLSelector fiqlSelector);

}
