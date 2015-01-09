package com.proptiger.userservice.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import com.proptiger.core.model.BaseModel;

/**
 * @author Rajeev Pandey
 * 
 */
@Entity
@Table(name = "user.persistent_logins")
public class UserPersistentLogin extends BaseModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Column(name = "username")
    private String userName;
    
    private String series;
    
    private String token;
    
    @Column(name = "last_used")
    private Date lastUsed;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    @PrePersist
    public void prePersist(){
        this.lastUsed = new Date();
    }
    
    @PreUpdate
    public void preUpdate(){
        this.lastUsed = new Date();
    }
    
    public PersistentRememberMeToken toPersistentRememberMeToken(){
        return new PersistentRememberMeToken(this.userName, this.series, this.token, this.lastUsed);
    }
}
