package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proptiger.core.model.cms.Company;

/**
 * 
 * @author azi
 * 
 */

public interface CompanyDao extends JpaRepository<Company, Integer> {
    @Query("SELECT DISTINCT C from Company C join C.coverages CC WHERE CC.localityId IN ?1 AND C.status = 'Active'")
    public List<Company> findBrokersForLocality(List<Integer> localityIds);
}
