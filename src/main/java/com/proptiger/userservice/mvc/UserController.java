package com.proptiger.userservice.mvc;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

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
import com.proptiger.core.dto.internal.user.RegisterUser;
import com.proptiger.core.enums.Application;
import com.proptiger.core.meta.DisableCaching;
import com.proptiger.core.model.user.User;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.service.ApplicationNameService;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.config.security.APIAccessLevel;
import com.proptiger.userservice.config.security.AccessLevel;
import com.proptiger.userservice.dto.ChangePassword;
import com.proptiger.userservice.service.CompanyUserService;
import com.proptiger.userservice.service.UserService;

/**
 * User APIs to get/register/update/delete a user entity
 * 
 * @author Rajeev Pandey
 * @author azi
 * 
 */
@Controller
@DisableCaching
public class UserController extends BaseController {

    @Value("${proptiger.url}")
    private String             proptigerUrl;

    @Autowired
    private UserService        userService;

    @Autowired
    private CompanyUserService companyUserService;

    @RequestMapping(method = RequestMethod.GET, value = "data/v1/registered")
    @ResponseBody
    public APIResponse isRegistered(String email) {
        return new APIResponse(userService.isRegistered(email));
    }

    @RequestMapping(value = "data/v1/entity/user/who-am-i", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse whoAmI() {
        return new APIResponse(userService.getWhoAmIDetail());
    }

    @RequestMapping(value = "data/v1/entity/user/change-password", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse changePassword(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo,
            @RequestBody ChangePassword changePassword) {
        userService.changePassword(userInfo, changePassword);
        return new APIResponse();
    }

    @RequestMapping(value = Constants.Security.REGISTER_URL, method = RequestMethod.POST)
    @ResponseBody
    public APIResponse register(@RequestBody RegisterUser register) {
        Application applicationType = ApplicationNameService.getApplicationTypeOfRequest();
        Integer userId = userService.register(register, applicationType);
        return new APIResponse(userService.getUserDetails(userId, applicationType, true));
    }

    @RequestMapping(value = "data/v1/entity/user", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getActiveUser(@ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser)
            throws IOException {
        User user = userService.getUserById(activeUser.getUserIdentifier());
        return new APIResponse(user);
    }

    @RequestMapping(value = "data/v1/entity/user", method = RequestMethod.GET, params = {"userId"})
    @ResponseBody
    @APIAccessLevel(level = {AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN})
    public APIResponse getUsersByUserIds(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser,
            @RequestParam(required = true) List<Integer> userId) throws IOException {
        return new APIResponse(super.filterFields(userService.getUsers(userId), new HashSet<String>()));
    }
    
    @RequestMapping(value = "data/v1/entity/user-details", method = RequestMethod.GET, params = {"userId"})
    @ResponseBody
    @APIAccessLevel(level = {AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN})
    public APIResponse getUserDetailsByUserIds(
            @RequestParam(required = true) List<Integer> userId) throws IOException {
        List<User> users = userService.getUserWithContactAuthProviderAndAttribute(userId);
        return new APIResponse(super.filterFields(users, new HashSet<String>()));
    }

    @RequestMapping(value = "data/v1/entity/user-details", method = RequestMethod.GET, params = {"email"})
    @ResponseBody
    @APIAccessLevel(level = {AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN})
    public APIResponse getUserDetailsByEmail(
            @RequestParam(required = true) String email) throws IOException {
        User user = userService.getUserByEmailWithContactAuthProviderAndAttribute(email);
        return new APIResponse(super.filterFields(user, new HashSet<String>()));
    }
    
    @RequestMapping(value = "data/v1/entity/user", method = RequestMethod.POST)
    @ResponseBody
    @APIAccessLevel(level = {AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN})
    public APIResponse createOrPatchUser(
            @RequestBody User user) throws IOException {
        return new APIResponse(super.filterFields(userService.createOrPatchUser(user), new HashSet<String>()));
    }
    
}