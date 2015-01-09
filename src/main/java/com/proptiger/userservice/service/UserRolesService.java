package com.proptiger.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.model.user.MasterRoles;
import com.proptiger.core.model.user.UserRoles;
import com.proptiger.userservice.dto.UserDetails;
import com.proptiger.userservice.repo.MasterRolesDao;
import com.proptiger.userservice.repo.UserDao;
import com.proptiger.userservice.repo.UserRolesDao;

/**
 * @author Rajeev Pandey
 *
 */
@Service
public class UserRolesService {
    @Autowired
    private UserRolesDao userRolesDao;
    
    @Autowired
    private MasterRolesDao masterRolesDao;
    
    @Autowired
    private UserDao userDao;

    /**
     * Update userRole of userId by admin
     * @param roles
     * @param userId
     * @param createdByUserId
     * @return
     */
    @Transactional
    public List<UserRoles> updateRolesOfUser(List<String> roles, Integer userId, Integer createdByUserId){
        List<UserRoles> userRolesToCreate = new ArrayList<UserRoles>();
        if(roles != null && !roles.isEmpty()){
            List<MasterRoles> masterRoles = masterRolesDao.findMasterRolesByNameIn(roles);
            if(roles.size() != masterRoles.size()){
                //in case if any invalid role name passed
                throw new BadRequestException("Invalid roles specified");
            }
            if(createdByUserId == null || createdByUserId <= 0){
                throw new BadRequestException("Invalid admin id");
            }
            Map<String, MasterRoles> roleNameToIdMapping = convertToRoleNameIdMap(masterRoles);
            List<String> existingRoles = userRolesDao.getUserRolesName(userId);
            Map<String, Boolean> existingRoleMap = new HashMap<String, Boolean>();
            for(String s: existingRoles){
                existingRoleMap.put(s, Boolean.TRUE);
            }
            for(String r: roles){
                //ignore duplicate roles
                if(existingRoleMap.get(r) == null){
                    UserRoles uRole = new UserRoles();
                    uRole.setRoleId(roleNameToIdMapping.get(r).getId());
                    uRole.setUserId(userId);
                    uRole.setCreatedBy(createdByUserId);
                    userRolesToCreate.add(uRole);
                }
            }
            if(!userRolesToCreate.isEmpty()){
                userRolesToCreate = userRolesDao.save(userRolesToCreate);
            }
        }
        return userRolesToCreate;
    }
    
    private Map<String, MasterRoles> convertToRoleNameIdMap(List<MasterRoles> masterRoles) {
        Map<String, MasterRoles> map = new HashMap<String, MasterRoles>();
        for(MasterRoles m: masterRoles){
            map.put(m.getName(), m);
        }
        return map;
    }

    public List<String> getUserRolesName(int userId){
        return userRolesDao.getUserRolesName(userId);
    }

    @Transactional
    public void deleteRoles(UserDetails userDetails, int adminUserId) {
        if(userDetails.getId() <= 0){
            throw new BadRequestException("Invalid user id specified");
        }
        if(userDetails.getId() == adminUserId){
            throw new BadRequestException("Sorry, you can not change your role");
        }
        if(userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()){
            List<MasterRoles> masterRoles = masterRolesDao.findMasterRolesByNameIn(userDetails.getRoles());
            List<Integer> masterRolesIds = new ArrayList<Integer>();
            for(MasterRoles m: masterRoles){
                masterRolesIds.add(m.getId());
            }
            if(!masterRolesIds.isEmpty()){
                userRolesDao.deleteRolesOfUserId(userDetails.getId(), masterRolesIds);
            }
           
        }
        
    }
    
}
