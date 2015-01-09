package com.proptiger.userservice.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.meta.DisableCaching;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.dto.UserDetails;
import com.proptiger.userservice.service.UserRolesService;
import com.proptiger.userservice.service.UserService;

/**
 * @author Rajeev Pandey
 *
 */
@Controller
@DisableCaching
public class UserRoleController extends BaseController {

    @Autowired
    private UserService      userService;

    @Autowired
    private UserRolesService userRolesService;

    
    @RequestMapping(value = "app/v1/user/role", method = RequestMethod.DELETE)
    @ResponseBody
    public APIResponse deleteRoleOfUserByAdmin(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser,
            @RequestBody UserDetails userDetails) {
        userRolesService.deleteRoles(userDetails, activeUser.getUserIdentifier());
        return new APIResponse(userService.getUserDetails(userDetails.getId(), activeUser.getApplicationType(), false));
    }
}
