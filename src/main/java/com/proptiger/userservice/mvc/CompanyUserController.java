package com.proptiger.userservice.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.model.companyuser.CompanyUser;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.FIQLSelector;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.service.CompanyUserService;

/**
 * @author Rajeev Pandey
 * 
 */
@Controller
public class CompanyUserController extends BaseController {

    @Autowired
    private CompanyUserService companyUserService;

    @RequestMapping(value = "data/v1/entity/user/company-user")
    @ResponseBody
    public APIResponse get(@ModelAttribute FIQLSelector selector , @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser) {
        CompanyUser agent = companyUserService.getAgent(activeUser.getUserIdentifier(), selector);
        return new APIResponse(super.filterFieldsFromSelector(agent, selector));
    }
    
    @RequestMapping(value = "data/v1/entity/company-users/{userId}")
    @ResponseBody
    public APIResponse getCompanyUsers(@ModelAttribute FIQLSelector selector , @PathVariable Integer userId) {
        CompanyUser agent = companyUserService.getAgentDetails(userId,selector);
        return new APIResponse(super.filterFieldsFromSelector(agent, selector));
    }
}
