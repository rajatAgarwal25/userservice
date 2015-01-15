package com.proptiger.userservice.mvc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.dto.internal.DashboardDto;
import com.proptiger.core.model.proptiger.Dashboard;
import com.proptiger.core.model.proptiger.DashboardWidgetMapping;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.FIQLSelector;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.service.DashboardService;

/**
 * This class provides the various API to interact with Dash board resource
 * 
 * @author Rajeev Pandey
 * 
 */
@Controller
@RequestMapping(value = "data/v1/entity/user/{userId}/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get all dashboard for user id
     * 
     * @param userId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getDashboards(
            @PathVariable Integer userId,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo,
            @ModelAttribute FIQLSelector fiqlSelector) {
        List<Dashboard> result = dashboardService.getAllByUserIdAndType(userInfo.getUserIdentifier(), fiqlSelector);
        return new APIResponse(result, result.size());
    }

    /**
     * This method get a dashboard for dashboard id
     * 
     * @param userId
     * @param dashboardId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{dashboardId}")
    @ResponseBody
    public APIResponse getDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        Dashboard result = dashboardService.getDashboardById(userInfo.getUserIdentifier(), dashboardId);
        return new APIResponse(result);
    }

    /**
     * Creates a dashboard
     * 
     * @param userId
     * @param dashboardDto
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public APIResponse createDashboard(
            @PathVariable Integer userId,
            @RequestBody(required = true) DashboardDto dashboardDto,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        dashboardDto.setUserId(userInfo.getUserIdentifier());
        Dashboard result = dashboardService.createDashboard(dashboardDto);
        return new APIResponse(result);
    }

    /**
     * This method updates a dashboard, but it will update only dashboard
     * properties and not the widget mapping part. To update widget's
     * positioning mapped with a dashboard use method
     * 
     * @param userId
     * @param dashboardId
     * @param dashboardDto
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{dashboardId}")
    @ResponseBody
    public APIResponse updateDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @RequestBody(required = true) DashboardDto dashboardDto,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        dashboardDto.setId(dashboardId);
        dashboardDto.setUserId(userInfo.getUserIdentifier());
        Dashboard dashboard = dashboardService.updateDashboard(dashboardDto);
        return new APIResponse(dashboard);
    }

    /**
     * Get all widgets associated with dashboard id
     * 
     * @param userId
     * @param dashboardId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{dashboardId}/widget")
    @ResponseBody
    public APIResponse getWidgetMappingWithDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        Dashboard dashboard = dashboardService.getDashboardById(userInfo.getUserIdentifier(), dashboardId);
        return new APIResponse(dashboard.getWidgets(), dashboard.getWidgets() == null ? 0 : dashboard
                .getWidgets().size());
    }

    /**
     * Get a widget association object for widget id and dashboard id
     * 
     * @param userId
     * @param dashboardId
     * @param widgetId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{dashboardId}/widget/{widgetId}")
    @ResponseBody
    public APIResponse getSingleWidgetMappingWithDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @PathVariable Integer widgetId,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        Dashboard dashboard = dashboardService.getDashboardById(userInfo.getUserIdentifier(), dashboardId);
        return new APIResponse(dashboard.getWidgets(), dashboard.getWidgets() == null ? 0 : dashboard
                .getWidgets().size());
    }

    /**
     * @param userId
     * @param dashboardId
     * @param dashboardWidgetMapping
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{dashboardId}/widget")
    @ResponseBody
    public APIResponse createWidgetMappingWithDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @RequestBody(required = true) DashboardWidgetMapping dashboardWidgetMapping,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        Dashboard dashboard = dashboardService.createSingleWidget(
                userInfo.getUserIdentifier(),
                dashboardId,
                dashboardWidgetMapping);
        return new APIResponse(dashboard);
    }

    /**
     * This method updates a provided widget id mapping with provided dashboard
     * id
     * 
     * @param userId
     * @param dashboardId
     * @param widgetId
     * @param dashboardDto
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{dashboardId}/widget/{widgetId}")
    @ResponseBody
    public APIResponse updateWidgetMappingWithDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @PathVariable Integer widgetId,
            @RequestBody(required = true) DashboardWidgetMapping dashboardWidgetMapping,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        Dashboard dashboard = dashboardService.updateWidgetMappingWithDashboard(
                userInfo.getUserIdentifier(),
                dashboardId,
                widgetId,
                dashboardWidgetMapping);
        return new APIResponse(dashboard);
    }

    /**
     * @param userId
     * @param dashboardId
     * @param widgetId
     * @param dashboardWidgetMapping
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{dashboardId}/widget/{widgetId}")
    @ResponseBody
    public APIResponse deleteWidgetFromDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @PathVariable Integer widgetId,
            @RequestBody(required = true) DashboardWidgetMapping dashboardWidgetMapping,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        Dashboard dashboard = dashboardService.deleteWidgetMappingWithDashboard(
                userInfo.getUserIdentifier(),
                dashboardId,
                widgetId,
                dashboardWidgetMapping);
        return new APIResponse(dashboard);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{dashboardId}")
    @ResponseBody
    public APIResponse deleteDashboard(
            @PathVariable Integer userId,
            @PathVariable Integer dashboardId,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser userInfo) {
        Dashboard deleted = dashboardService.deleteDashboard(userInfo.getUserIdentifier(), dashboardId);
        return new APIResponse(deleted);
    }
}
