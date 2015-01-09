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
import com.proptiger.core.meta.DisableCaching;
import com.proptiger.core.model.user.UserPreference;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.service.UserPreferenceService;

/**
 * B2b User Detail Controller
 * 
 * @author Azitabh Ajit
 * 
 */

@Controller
@RequestMapping
@DisableCaching
public class UserPreferenceController extends BaseController {
    @Autowired
    UserPreferenceService b2bUserPreferenceService;

    @RequestMapping(value = "/data/v1/entity/user/preference", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse createUserPreference(
            @RequestBody UserPreference preference,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser user) throws Exception {
        return new APIResponse(b2bUserPreferenceService.createUserPreference(preference, user.getUserIdentifier()));
    }

    @RequestMapping(value = "/data/v1/entity/user/preference/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public APIResponse updateUserPreference(
            @PathVariable Integer id,
            @RequestBody UserPreference preference,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser user) throws Exception {
        preference.setId(id);
        return new APIResponse(b2bUserPreferenceService.updateUserPreference(preference, user.getUserIdentifier()));
    }

    @RequestMapping(value = "/data/v1/entity/user/preference", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getUserPreference(@ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser user)
            throws Exception {
        return new APIResponse(b2bUserPreferenceService.getUserPreferences(user.getUserIdentifier()));
    }
}