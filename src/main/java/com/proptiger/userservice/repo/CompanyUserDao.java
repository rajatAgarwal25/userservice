package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proptiger.core.enums.ActivationStatus;
import com.proptiger.core.model.cms.Company;
import com.proptiger.core.model.companyuser.CompanyUser;

/**
 * @author Rajeev Pandey
 * @author azi
 * 
 */
public interface CompanyUserDao extends JpaRepository<CompanyUser, Integer> {
    public List<CompanyUser> findByCompanyIdAndStatus(Integer companyId, ActivationStatus status);

    @Query("select CU from CompanyUser CU join fetch CU.companyCoverages CC join fetch CC.locality L where CU.userId = ?1")
    public CompanyUser findLocalitiesByUserId(int agentId);

    public CompanyUser findByUserId(Integer companyUserId);
    
    public List<CompanyUser> findByUserIdIn(List<Integer> companyUserId);

    @Query("select C from CompanyUser CU join CU.company C where CU.userId in (?1)")
    public List<Company> findByUserId(List<Integer> agentIds);

    @Query("select CU from CompanyUser CU join fetch CU.company C join fetch CU.academicQualification join fetch CU.companyCoverages where CU.userId = ?1")
    public CompanyUser findFullByUserId(Integer userId);

    @Query("select CU from CompanyUser CU join fetch CU.company C where CU.userId = ?1 ")
    public List<CompanyUser> findCompanyUsersByUserId(Integer userId);

    @Query("select CU from CompanyUser CU where CU.left > ?1 and CU.right < ?2  and CU.companyId=?3 order by CU.left asc")
    public List<CompanyUser> getCompanyUsersInLeftRightRangeInCompany(int left, int right, int companyId);
    
    public List<CompanyUser> findByParentIdAndCompanyId(int parentId, int companyId);
}
