package com.proptiger.userservice.dto;

/**
 * Reset password data to generate mail template
 * @author Rajeev Pandey
 *
 */
public class ResetPasswordTemplateData {
    private String userName;
    private String changePasswordUrl;

    public ResetPasswordTemplateData(String userName, String changePasswordUrl) {
        super();
        this.userName = userName;
        this.changePasswordUrl = changePasswordUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getChangePasswordUrl() {
        return changePasswordUrl;
    }

}