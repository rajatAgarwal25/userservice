package com.proptiger.userservice.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.ActivationStatus;
import com.proptiger.core.enums.ResourceType;
import com.proptiger.core.enums.ResourceTypeAction;
import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.exception.ResourceNotAvailableException;
import com.proptiger.core.model.cms.Company;
import com.proptiger.core.model.cms.CompanyCoverage;
import com.proptiger.core.model.cms.Locality;
import com.proptiger.core.model.companyuser.CompanyUser;
import com.proptiger.core.pojo.FIQLSelector;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.dto.UserDetails;
import com.proptiger.userservice.repo.CompanyDao;
import com.proptiger.userservice.repo.CompanyUserDao;

/**
 * Service class to get agent related details
 * 
 * @author Rajeev Pandey
 * 
 */
@Service
public class CompanyUserService {
    
    @Autowired
    private CompanyDao     companyDao;
    @Autowired
    private CompanyUserDao companyUserDao;
    
    /**
     * finds all broker companies which deal in one of the localities
     * 
     * @param localityIds
     * @return {@link Company} {@link List}
     */
    public List<Company> getCompaniesForLocalitiesOrUserIds(List<Integer> localityIds, List<Integer> userIds) {
        if(localityIds != null && !localityIds.isEmpty()){
            return companyDao.findBrokersForLocality(localityIds);
        }
        else if(userIds != null && !userIds.isEmpty()){
            return companyUserDao.findByUserId(userIds);
        }
        else{
            throw new BadRequestException("Either localityIds or userIds is mandatory");
        }
    }

    /**
     * finds all users for a company
     * 
     * @param companyIds
     * @return {@link CompanyUser} {@link List}
     */
    public List<CompanyUser> getCompanyUsersForCompanies(Integer companyId) {
        return companyUserDao.findByCompanyIdAndStatus(companyId, ActivationStatus.Active);
    }

    public CompanyUser getAgentDetails(Integer userId, FIQLSelector selector) {
        CompanyUser companyUser = companyUserDao.findFullByUserId(userId);

        if (companyUser == null) {
            throw new ResourceNotAvailableException(ResourceType.COMPANY_USER, ResourceTypeAction.GET);
        }

        Set<String> fields = selector.getFieldSet();
        if (fields.contains("localities")) {
            CompanyUser companyUserFull = companyUserDao.findLocalitiesByUserId(userId);
            List<Locality> localities = new ArrayList<Locality>();
            for (CompanyCoverage companyCoverage : companyUserFull.getCompanyCoverages()) {
                localities.add(companyCoverage.getLocality());
            }
            companyUser.setLocalities(localities);
        }

        return companyUser;
    }

    public List<CompanyUser> getCompanyUsers(Integer userId) {
        List<CompanyUser> companyUser = companyUserDao.findCompanyUsersByUserId(userId);
        return companyUser;
    }

    /**
     * Update left right values in hierarchy system for a company
     * @param user
     * @param activeUser
     */
    public void updateLeftRightOfInCompany(UserDetails user, ActiveUser activeUser) {
        if (user.getParentId() != null && !(user.getParentId() <= 0) && SecurityContextUtils.isAdmin(activeUser)) {
            CompanyUser companyUser = companyUserDao.findByUserId(user.getId());
            if (companyUser == null) {
                throw new BadRequestException("User id is not in hierarchy");
            }
            int companyId = companyUser.getCompanyId();
            List<CompanyUser> rootCompanyUsers = companyUserDao.findByParentIdAndCompanyId(0, companyId);
            List<CompanyUser> toUpdate = new ArrayList<CompanyUser>();
            for (CompanyUser cu : rootCompanyUsers) {
                int[] left = { 1 };
                updateChildren(cu, left, toUpdate);
            }
            companyUserDao.save(toUpdate);
        }
    }

    private void updateChildren(CompanyUser root, int[] left, List<CompanyUser> toUpdate) {
        int val = left[0];
        root.setLeft(val);
        toUpdate.add(root);
        List<CompanyUser> companyUsers = companyUserDao.findByParentIdAndCompanyId(
                root.getUserId(),
                root.getCompanyId());

        if (!companyUsers.isEmpty()) {
            for (CompanyUser c : companyUsers) {
                left[0] = left[0] + 1;
                updateChildren(c, left, toUpdate);
            }
        }
        left[0] = left[0] + 1;
        val = left[0];
        root.setRight(val);
    }

    @Transactional
    public void updateParentDetail(UserDetails userDetails) {
        if ((userDetails.getParentId() != null && !(userDetails.getParentId() <= 0))) {
            List<Integer> userIds = Arrays.asList(userDetails.getId(), userDetails.getParentId());
            List<CompanyUser> companyUsers = companyUserDao.findByUserIdIn(userIds);
            if (companyUsers.isEmpty() || companyUsers.size() != 2) {
                throw new BadRequestException("User id and parent id are not in hierarchy");
            }
            else if (companyUsers.get(0).getCompanyId() != companyUsers.get(1).getCompanyId()) {
                throw new BadRequestException("User and parent are not in same company");
            }
            CompanyUser companyUserToUpdate = null;
            CompanyUser parentCompanyUser = null;
            if (companyUsers.get(0).getUserId() == userDetails.getId()) {
                companyUserToUpdate = companyUsers.get(0);
                parentCompanyUser = companyUsers.get(1);
            }
            else {
                companyUserToUpdate = companyUsers.get(1);
                parentCompanyUser = companyUsers.get(0);
            }
            if (parentCompanyUser.getLeft() > companyUserToUpdate.getLeft() && parentCompanyUser.getRight() < companyUserToUpdate
                    .getRight()) {
                parentCompanyUser.setParentId(0);
                companyUserToUpdate.setParentId(parentCompanyUser.getUserId());
            }
            else {
                // these nodes were as seperate nodes so just update
                companyUserToUpdate.setParentId(userDetails.getParentId());
            }
            companyUserDao.save(companyUsers);
        }

    }
}
