package com.proptiger.userservice.model;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.proptiger.core.model.BaseModel;

/**
 * @author Rajeev Pandey
 *
 */
@Entity
@Table(name = "user.user_otps")
public class UserOTP extends BaseModel{
    public static final int EXPIRES_IN_MINUTES = 30;

    private static final long serialVersionUID = -5831632111030084121L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "otp")
    private Integer otp;
    
    @Column(name = "expires_at")
    private Date expiresAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getOtp() {
        return otp;
    }

    public void setOtp(Integer otp) {
        this.otp = otp;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    @PrePersist
    public void prePersist(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, EXPIRES_IN_MINUTES);
        expiresAt = cal.getTime();
    }
}
