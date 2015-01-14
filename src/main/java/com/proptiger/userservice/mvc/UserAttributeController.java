package com.proptiger.userservice.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.model.user.UserAttribute;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.service.UserAttributeService;

/**
 * @author Nikhil Singhal
 */

@Controller
public class UserAttributeController extends BaseController {

    @Autowired
    private UserAttributeService userAttributeService;

    @RequestMapping(value = "data/v1/entity/user/attribute", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse createUserAttributes(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser,
            @RequestBody UserAttribute userAttribute) {

        return new APIResponse(userAttributeService.createAttribute(activeUser, userAttribute));
    }

    @RequestMapping(value = "data/v1/entity/user/attribute/{attributeId}", method = RequestMethod.PUT)
    @ResponseBody
    public APIResponse updateUserAttributes(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser,
            @PathVariable Integer attributeId,
            @RequestBody UserAttribute userAttribute) {

        return new APIResponse(userAttributeService.updateAttribute(activeUser, attributeId, userAttribute.getAttributeValue()));
    }

}
