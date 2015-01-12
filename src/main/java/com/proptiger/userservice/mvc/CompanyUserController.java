package com.proptiger.userservice.mvc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.model.cms.Company;
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
    public APIResponse get(
            @ModelAttribute FIQLSelector selector,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser) {
        CompanyUser agent = companyUserService.getAgentDetails(activeUser.getUserIdentifier(), selector);
        return new APIResponse(super.filterFieldsFromSelector(agent, selector));
    }

    @RequestMapping(value = "data/v1/entity/company-users/{userId}")
    @ResponseBody
    public APIResponse getCompanyUsers(@ModelAttribute FIQLSelector selector, @PathVariable Integer userId) {
        CompanyUser agent = companyUserService.getAgentDetails(userId, selector);
        return new APIResponse(super.filterFieldsFromSelector(agent, selector));
    }

    @RequestMapping(value = "data/v1/entity/company", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getCompanyForLocalityId(
            @RequestParam(value = "localityIds") List<Integer> localityIds,
            @RequestParam(value = "userIds") List<Integer> userIds,
            @ModelAttribute FIQLSelector selector) {
        List<Company> list = companyUserService.getCompaniesForLocalitiesOrUserIds(localityIds, userIds);
        return new APIResponse(super.filterFieldsFromSelector(list, selector));
    }

    @RequestMapping(value = "data/v1/entity/company/{companyId}/company-users", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getCompanyUsersInCompanyId(@PathVariable Integer companyId, @ModelAttribute FIQLSelector selector) {
        List<CompanyUser> list = companyUserService.getCompanyUsersForCompanies(companyId);
        return new APIResponse(super.filterFieldsFromSelector(list, selector));
    }
}
