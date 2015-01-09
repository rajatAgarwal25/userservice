package com.proptiger.userservice.dto;

import java.util.List;

/**
 * @author Rajeev Pandey
 *
 */
public class UserDetails extends RegisterUser {

    private static final long serialVersionUID = 255485230112121756L;
    
    private String            oldPassword;
    //this is supposed to be user id of users table
    private Integer parentId;
    private List<String> roles;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    
}
