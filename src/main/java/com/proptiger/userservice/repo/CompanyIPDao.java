package com.proptiger.userservice.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proptiger.core.model.CompanyIP;

/**
 * @author Rajeev Pandey
 *
 */
public interface CompanyIPDao extends JpaRepository<CompanyIP, Integer>{
    List<CompanyIP> findByCompanyIdIn(Set<Integer> companyIds);
}
