package com.proptiger.userservice.mvc;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.user.CustomUser;
import com.proptiger.core.meta.DisableCaching;
import com.proptiger.core.model.user.User;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.userservice.config.security.APIAccessLevel;
import com.proptiger.userservice.config.security.AccessLevel;
import com.proptiger.userservice.service.CompanyUserService;
import com.proptiger.userservice.service.UserService;

/**
 * User controller that fetch user details by id and email and does not require
 * login, and supported from internal IP only, means from white listed IP.
 * 
 * @author Rajeev Pandey
 *
 */
@Controller
@DisableCaching
public class AppUserController extends BaseController {

    @Value("${proptiger.url}")
    private String             proptigerUrl;

    @Autowired
    private UserService        userService;

    @Autowired
    private CompanyUserService companyUserService;

    @RequestMapping(value = "app/v1/user", method = RequestMethod.GET, params = { "userId" })
    @ResponseBody
    @APIAccessLevel(level = { AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN })
    public APIResponse getUsersByUserIds(@RequestParam(required = true) List<Integer> userId) throws IOException {
        List<User> list = userService.getUsersByIds(userId);
        return new APIResponse(list);
    }

    @RequestMapping(value = "app/v1/user-details", method = RequestMethod.GET, params = { "userId" })
    @ResponseBody
    @APIAccessLevel(level = { AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN })
    public APIResponse getUserDetailsByUserIds(@RequestParam(required = true) List<Integer> userId) throws IOException {
        List<User> users = userService.getUserWithContactAuthProviderAndAttribute(userId);
        return new APIResponse(users);
    }

    @RequestMapping(value = "app/v1/user-details", method = RequestMethod.GET, params = { "email" })
    @ResponseBody
    @APIAccessLevel(level = { AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN })
    public APIResponse getUserDetailsByEmail(@RequestParam(required = true) String email) throws IOException {
        User user = userService.getUserByEmailWithContactAuthProviderAndAttribute(email);
        return new APIResponse(user);
    }

    
    @RequestMapping(value = "data/v1/entity/user", method = RequestMethod.POST)
    @ResponseBody
    @APIAccessLevel(level = {AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN})
    public APIResponse createOrPatchUser(
            @RequestBody User user) throws IOException {
        User u = userService.createOrPatchUser(user);
        return new APIResponse(u);
    }
    
    @RequestMapping(value = "app/v1/user/details-by-email", method = RequestMethod.GET)
    @ResponseBody
    @APIAccessLevel(level = {AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN})
    public APIResponse getUserDetailsByEmailId(@RequestParam String email) {
        CustomUser customUser = userService.getUserDetailsByEmail(email);
        return new APIResponse(customUser);
    }
    
}
