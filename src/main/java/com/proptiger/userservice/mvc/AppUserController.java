package com.proptiger.userservice.mvc;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.dto.internal.user.CustomUser;
import com.proptiger.core.model.user.User;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.dto.ChangePassword;
import com.proptiger.userservice.dto.UserDetails;
import com.proptiger.userservice.service.CompanyUserService;
import com.proptiger.userservice.service.UserService;
import com.proptiger.userservice.service.UserService.UserCommunicationType;

/**
 * @author Rajeev Pandey
 *
 */
@Controller
public class AppUserController extends BaseController{

    @Value("${proptiger.url}")
    private String      proptigerUrl;

    @Autowired
    private UserService userService;
    
    @Autowired
    private CompanyUserService companyUserService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/app/v1/user/details")
    @ResponseBody
    public APIResponse getUserDetails(@ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser) {
        return new APIResponse(userService.getUserDetails(
                activeUser.getUserIdentifier(),
                activeUser.getApplicationType(), true));
    }
    
    @RequestMapping(value = "app/v1/reset-password", method = RequestMethod.POST, params = {"email"})
    @ResponseBody
    public APIResponse resetPassword(
            @RequestParam String email) {
        Object message = userService.processResetPasswordRequest(email);
        return new APIResponse(message);
    }
    
    @RequestMapping(value = "app/v1/reset-password", method = RequestMethod.POST, params = {"token"})
    @ResponseBody
    public APIResponse resetPasswordUsingToken(
            @RequestParam String token,
            @RequestBody ChangePassword changePassword) {
        CustomUser customUser = userService.resetPasswordUsingToken(token, changePassword);
        return new APIResponse(customUser);
    }

    /**
     * Accesible by users having role UserRole.ADMIN_BACKEND
     * 
     * @param email
     * @return
     */
    @RequestMapping(value = "app/v1/user/details-by-email", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getUserDetailsByEmailId(@RequestParam String email) {
        CustomUser customUser = userService.getUserDetailsByEmail(email);
        return new APIResponse(customUser);
    }
    
    @RequestMapping(value = Constants.Security.USER_VALIDATE_API, method = RequestMethod.GET)
    @ResponseBody
    public void validateUserCommunicationDetails(
            @RequestParam UserCommunicationType type,
            @RequestParam String token,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        userService.validateUserCommunicationDetails(type, token);
        response.sendRedirect(proptigerUrl + "?flag=email_valid");
    }

    @RequestMapping(value = "app/v1/entity/user/details", method = RequestMethod.PUT)
    @ResponseBody
    public APIResponse updateUserDetails(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser,
            @RequestBody UserDetails user) throws IOException {
        User u = userService.updateUserDetails(user, activeUser);
        companyUserService.updateLeftRightOfInCompany(user, activeUser);
        return new APIResponse(userService.getUserDetails(
                u.getId(),
                activeUser.getApplicationType(), false));
    }
    
    @RequestMapping(value = "app/v1/entity/user/child", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getChild(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser) throws IOException {
        return new APIResponse(userService.getChildHeirarchy(activeUser));
    }
}
