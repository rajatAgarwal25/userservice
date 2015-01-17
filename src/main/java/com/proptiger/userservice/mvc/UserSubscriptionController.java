package com.proptiger.userservice.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.service.UserSubscriptionService;

/**
 * @author Rajeev Pandey
 *
 */
@Controller
public class UserSubscriptionController extends BaseController{

    @Autowired
    private UserSubscriptionService userSubscriptionService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/data/v1/entity/user/subscription/permission")
    @ResponseBody
    public APIResponse getUserSubscriptionPermissions(@ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser){
        return new APIResponse(userSubscriptionService.getUserAppSubscriptionDetails(activeUser.getUserIdentifier()));
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/data/v1/entity/user/subscription/mapping")
    @ResponseBody
    public APIResponse getUserSubscriptionMapping(@ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser){
        return new APIResponse(userSubscriptionService.getUserSubscriptionMappingList(activeUser.getUserIdentifier()));
    }
}
