package com.proptiger.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cxf.jaxrs.ext.search.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.dto.internal.DashboardDto;
import com.proptiger.core.enums.ResourceType;
import com.proptiger.core.enums.ResourceTypeAction;
import com.proptiger.core.enums.WidgetDisplayStatus;
import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.exception.ConstraintViolationException;
import com.proptiger.core.exception.DuplicateNameResourceException;
import com.proptiger.core.exception.DuplicateResourceException;
import com.proptiger.core.exception.ResourceNotAvailableException;
import com.proptiger.core.model.proptiger.Dashboard;
import com.proptiger.core.model.proptiger.Dashboard.DashboardType;
import com.proptiger.core.model.proptiger.DashboardWidgetMapping;
import com.proptiger.core.pojo.FIQLSelector;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.repo.DashboardDao;
import com.proptiger.userservice.repo.DashboardWidgetMappingDao;

/**
 * Dashboard service class to provide CRUD operation over Dashboard resource
 * 
 * @author Rajeev Pandey
 * 
 */
@Service
public class DashboardService {

    private static Logger             logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private DashboardDao              dashboardDao;

    @Autowired
    private DashboardWidgetMappingDao dashboardWidgetMappingDao;

    /**
     * Finds all dashboard for given user id and dashboardType (PORTFOLIO or B2B)
     * 
     * @param fiqlSelector
     * @param userId
     * @return
     */
    @Transactional
    public List<Dashboard> getAllByUserIdAndType(Integer userId, FIQLSelector fiqlSelector) {
        logger.debug("Finding all dashboards for userid {}", userId);
        if (fiqlSelector.getFilters() == null) {
            fiqlSelector.addAndConditionToFilter("dashboardType==" + "PORTFOLIO");
        }
        fiqlSelector.addAndConditionToFilter("userId==" + userId);
        List<Dashboard> result;
        
        try {
            result = dashboardDao.getDashboards(fiqlSelector);
        }
        catch (PropertyNotFoundException e ) {
            throw new BadRequestException(ResponseErrorMessages.BAD_REQUEST);
        }

        if (result != null && result.size() == 0) {
            logger.debug("creating default dashboard and widgets as of admin for user {}", userId);
            /*
             * No dashboard exists for this user, need to create defaule
             * dashboard and and dashboard widget mapping by taking input from
             * admin's mapping
             */
            
            DashboardType dashboardType = extractDashboardType(fiqlSelector);
            List<Dashboard> adminsDashboard = dashboardDao.findByUserIdAndDashboardType(Constants.ADMIN_USER_ID, dashboardType);
            logger.debug("Dasboard and widgets for admin is {}", adminsDashboard);
            /* --need to create copy for current user --
             * For the dashboardType given in GET request 
             * corresponding dashboardType Admin data will be copied
             */
            if (adminsDashboard != null && !adminsDashboard.isEmpty()) {
                for (Dashboard dashboard : adminsDashboard) {
                    DashboardDto dashboardDto = new DashboardDto();
                    dashboardDto.setName(dashboard.getName());
                    dashboardDto.setTotalColumn(dashboard.getTotalColumn());
                    dashboardDto.setTotalRow(dashboard.getTotalRow());
                    dashboardDto.setUserId(userId);
                    dashboardDto.setDashboardType(dashboard.getDashboardType());
                    List<DashboardWidgetMapping> widgetMappintToCreate = new ArrayList<>();
                    for (DashboardWidgetMapping widgetMapping : dashboard.getWidgets()) {
                        DashboardWidgetMapping dashboardWidgetMapping = new DashboardWidgetMapping();
                        dashboardWidgetMapping.setStatus(widgetMapping.getStatus());
                        dashboardWidgetMapping.setWidgetColumnPosition(widgetMapping.getWidgetColumnPosition());
                        dashboardWidgetMapping.setWidgetId(widgetMapping.getWidgetId());
                        dashboardWidgetMapping.setWidgetRowPosition(widgetMapping.getWidgetRowPosition());
                        widgetMappintToCreate.add(dashboardWidgetMapping);
                    }
                    dashboardDto.setWidgets(widgetMappintToCreate);
                    createDashboard(dashboardDto);
                }
                // again retrieving dashboards for user after creation
                result = dashboardDao.findByUserIdAndDashboardType(userId, dashboardType);
            }
        }
        return result;
    }

    /**
     * @param fiqlSelector
     * @return DashboardType
     */
    private DashboardType extractDashboardType(FIQLSelector fiqlSelector) {
        Pattern responsePattern = Pattern.compile("dashboardType==(\\w*)");
        Matcher m = responsePattern.matcher(fiqlSelector.getFilters());
        DashboardType dashboardType = null;
        if (m.find()) {
             dashboardType = DashboardType.valueOf(m.group(1));
        }
        if (dashboardType == null) {
            throw new IllegalArgumentException("Invalid Dashboard Type");
        }
        
        return dashboardType;
    }

    /**
     * Finds dashboard for given user id and dashboard id
     * 
     * @param userId
     * @param dashboardId
     * @throws ResourceNotAvailableException
     * @return
     */
    @Transactional(readOnly = true)
    public Dashboard getDashboardById(Integer userId, Integer dashboardId) {
        logger.debug("Finding dashboard id {} for userid {}", userId, dashboardId);
        Dashboard result = dashboardDao.findByIdAndUserId(dashboardId, userId);
        if (result == null) {
            logger.error("Dashboard id {} not found for userid {}", dashboardId, userId);
            throw new ResourceNotAvailableException(ResourceType.DASHBOARD, ResourceTypeAction.GET);
        }
        return result;
    }

    /**
     * Find a dashboard widget mapping
     * 
     * @param userId
     * @param dashboardId
     * @param widgetId
     * @return
     */
    @Transactional(readOnly = true)
    public DashboardWidgetMapping getSingleWidgetMapping(Integer userId, Integer dashboardId, Integer widgetId) {
        Dashboard dashboard = getDashboardById(userId, dashboardId);
        DashboardWidgetMapping toFind = null;
        if (dashboard.getWidgets() != null) {
            for (DashboardWidgetMapping present : dashboard.getWidgets()) {
                if (present.getId().equals(widgetId)) {
                    toFind = present;
                    break;
                }
            }
        }
        if (toFind == null) {
            logger.error("Widget id {} not found for dashboard id {}", widgetId, dashboardId);
            throw new ResourceNotAvailableException(ResourceType.WIDGET, ResourceTypeAction.GET);
        }
        return toFind;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.proptiger.data.service.portfolio.AbstractService#preProcessCreate
     * (com.proptiger.data.model.resource.Resource)
     */
    private  void preProcessCreate(Dashboard resource) {
        resource.setId(null);
        Dashboard toCreate = (Dashboard) resource;
        Dashboard dashboardPresent = dashboardDao.findByNameAndUserIdAndDashboardType(toCreate.getName(), toCreate.getUserId(), toCreate.getDashboardType());
        if (dashboardPresent != null) {
            logger.error("Duplicate resource {}", dashboardPresent.getId());
            throw new DuplicateNameResourceException("Resource with same name exist");
        }
    }

    /**
     * Updating a dashboard resource
     * 
     * @param dashboardDto
     * @throws ``
     * @return
     */
    @Transactional(rollbackFor = ResourceNotAvailableException.class)
    public Dashboard updateDashboard(DashboardDto dashboardDto) {
        logger.debug("Updating dashboard id {} for userid {}", dashboardDto.getId(), dashboardDto.getUserId());
        Dashboard dashboard = Dashboard.getBuilder(dashboardDto.getName(), dashboardDto.getUserId())
                .setTotalColumns(dashboardDto.getTotalColumn()).setTotalRows(dashboardDto.getTotalRow())
                .setId(dashboardDto.getId()).build();
        Dashboard updated = update(dashboard);
        if (dashboardDto.getWidgets() != null) {
            for (DashboardWidgetMapping mapping : dashboardDto.getWidgets()) {
                updateWidgetMappingWithDashboard(
                        dashboardDto.getUserId(),
                        dashboardDto.getId(),
                        mapping.getWidgetId(),
                        mapping);
            }
        }
        updated = getDashboardById(dashboardDto.getUserId(), dashboardDto.getId());
        return updated;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.proptiger.data.service.portfolio.AbstractService#preProcessUpdate
     * (com.proptiger.data.model.resource.Resource)
     */
    private Dashboard preProcessUpdate(Dashboard resource) {
        Dashboard toUpdate = (Dashboard) resource;
        Dashboard dashboardPresent = dashboardDao.findOne(toUpdate.getId());
        if (dashboardPresent == null) {
            logger.error("Dashboard id {} not found", toUpdate.getId());
            throw new ResourceNotAvailableException(ResourceType.DASHBOARD, ResourceTypeAction.UPDATE);
        }
        if (toUpdate.getWidgets() != null) {
            for (DashboardWidgetMapping mapping : toUpdate.getWidgets()) {
                if (mapping.getWidgetId() == null) {
                    throw new IllegalArgumentException("Invalid widget Id");
                }
            }
        }
        return dashboardPresent;
    }

    /**
     * Deletes a dashboard resource from data store and all its association with
     * widgets
     * 
     * @throws ResourceNotAvailableException
     * @param userId
     * @param dashboardId
     * @return
     */
    @Transactional(rollbackFor = ResourceNotAvailableException.class)
    public Dashboard deleteDashboard(Integer userId, Integer dashboardId) {
        Dashboard deleted = getDashboardById(userId, dashboardId);
        List<DashboardWidgetMapping> widgetMappings = deleted.getWidgets();
        /*
         * First need to delete widgets association with this dashboard object
         */
        dashboardWidgetMappingDao.delete(widgetMappings);
        /*
         * Now dashboard object can be deleted
         */
        dashboardDao.delete(deleted);
        return deleted;
    }

    /**
     * Creates a dashboard for particular dashboardType
     * @param dashboardDto
     * @return
     */
    @Transactional(rollbackFor = ConstraintViolationException.class)
    public Dashboard createDashboard(DashboardDto dashboardDto) {
        logger.debug("Creating dashboard for userid {}", dashboardDto.getUserId());
        Dashboard dashboard = Dashboard.getBuilder(dashboardDto.getName(), dashboardDto.getUserId())
                .setTotalColumns(dashboardDto.getTotalColumn()).setTotalRows(dashboardDto.getTotalRow()).setDashboardType(dashboardDto.getDashboardType()).build();
        Dashboard created = create(dashboard);
        /*
         * creating dashboard and widget association
         */
        List<DashboardWidgetMapping> widgetMappings = createDashboardWidgetMappings(
                dashboardDto.getUserId(),
                created.getId(),
                dashboardDto.getWidgets());
        created.setWidgets(widgetMappings);
        return created;
    }

    /**
     * Creating dashboard and widget mappings
     * 
     * @param created
     */
    @Transactional(rollbackFor = ConstraintViolationException.class)
    private List<DashboardWidgetMapping> createDashboardWidgetMappings(
            Integer userId,
            Integer dashboardId,
            List<DashboardWidgetMapping> toCreate) {
        logger.debug("Creating dashboard widget mapping for user {} and dashboard {}", userId, dashboardId);
        List<DashboardWidgetMapping> createdWidgetsMapping = new ArrayList<DashboardWidgetMapping>();
        if (toCreate != null) {
            for (DashboardWidgetMapping mapping : toCreate) {
                // explicitly setting id to null as it would be auto created
                mapping.setDashboardId(dashboardId);
                if (mapping.getStatus() == null) {
                    mapping.setStatus(WidgetDisplayStatus.MAX);
                }

            }
            /*
             * Saving newly created dashboard and widget mapping
             */
            try {
                createdWidgetsMapping = dashboardWidgetMappingDao.save(toCreate);
            }
            catch (Exception e) {
                logger.error("Exception while creating dashboard widget mapping-" + e.getMessage());
                throw new ConstraintViolationException(e.getMessage(), e);
            }
        }

        return createdWidgetsMapping;
    }

    private Dashboard create(Dashboard resource) {
        Dashboard toCreate = (Dashboard) resource;
        preProcessCreate(toCreate);

        Dashboard created = null;
        try {
            created = dashboardDao.save(toCreate);
        }
        catch (Exception exception) {
            throw new ConstraintViolationException(exception.getMessage(), exception);
        }
        logger.debug("Created dashboard id {} for userid {}", created.getId(), toCreate.getUserId());
        return  created;
    }

    private Dashboard update(Dashboard resource) {
        Dashboard dashboard = (Dashboard) resource;
        Dashboard updated = preProcessUpdate(dashboard);
        updated.update(dashboard.getName(), dashboard.getTotalColumn(), dashboard.getTotalRow());
        return updated;
    }

    /**
     * This method is adding a widget association with dashboard, it will not
     * over right the existing mapping, rather it will add a new mapping in
     * existing mappings
     * 
     * @param userId
     * @param dashboardId
     * @param dashboardWidgetMapping
     * @return
     */
    @Transactional(rollbackFor = ResourceNotAvailableException.class)
    public Dashboard createSingleWidget(
            Integer userId,
            Integer dashboardId,
            DashboardWidgetMapping dashboardWidgetMapping) {
        dashboardWidgetMapping.setId(null);
        Dashboard dashboard = getDashboardById(userId, dashboardId);
        if (dashboard.getWidgets() != null) {
            for (DashboardWidgetMapping existingMapping : dashboard.getWidgets()) {
                if (existingMapping.getWidgetId().equals(dashboardWidgetMapping.getWidgetId())) {
                    logger.error(
                            "Duplicate mapping of dashboard id {} and widgetId {}",
                            dashboardId,
                            existingMapping.getWidgetId());
                    throw new DuplicateResourceException("Mapping of dashboard id and widget id exist");
                }
            }
        }
        dashboardWidgetMapping.setDashboardId(dashboardId);
        if (dashboardWidgetMapping.getStatus() == null) {
            dashboardWidgetMapping.setStatus(WidgetDisplayStatus.MAX);
        }
        DashboardWidgetMapping createdMapping = dashboardWidgetMappingDao.save(dashboardWidgetMapping);
        dashboard.addWidget(createdMapping);
        return dashboard;
    }

    /**
     * This method updates a dashboard widget mapping attribute
     * 
     * @param userId
     * @param dashboardId
     * @param widgetId
     * @param dashboardWidgetMapping
     * @return
     */
    @Transactional(rollbackFor = ResourceNotAvailableException.class)
    public Dashboard updateWidgetMappingWithDashboard(
            Integer userId,
            Integer dashboardId,
            Integer widgetId,
            DashboardWidgetMapping dashboardWidgetMapping) {
        Dashboard dashboard = getDashboardById(userId, dashboardId);
        DashboardWidgetMapping existingMapping = getDashboardWidgetMapping(dashboardId, widgetId);
        if (existingMapping == null) {
            throw new ResourceNotAvailableException(ResourceType.WIDGET, ResourceTypeAction.UPDATE);
        }
        existingMapping.update(
                dashboardWidgetMapping.getWidgetRowPosition(),
                dashboardWidgetMapping.getWidgetColumnPosition(),
                dashboardWidgetMapping.getStatus());
        dashboard = getDashboardById(userId, dashboardId);
        return dashboard;
    }

    /**
     * This method get a dashboard widget mapping based on dashboard id and
     * widget id
     * 
     * @param dashboardId
     * @param widgetId
     * @return
     */
    private DashboardWidgetMapping getDashboardWidgetMapping(Integer dashboardId, Integer widgetId) {
        DashboardWidgetMapping existingMapping = dashboardWidgetMappingDao.findByDashboardIdAndWidgetId(
                dashboardId,
                widgetId);
        if (existingMapping == null) {
            logger.error("DashboardWidgetMapping not found for dashboardId {} and widgetId {}", dashboardId, widgetId);
            throw new ResourceNotAvailableException(ResourceType.WIDGET, ResourceTypeAction.GET);
        }
        return existingMapping;
    }

    /**
     * This method deletes a widget association with dashboard
     * 
     * @param userId
     * @param dashboardId
     * @param widgetId
     * @param dashboardWidgetMapping
     * @return
     */
    @Transactional(rollbackFor = ResourceNotAvailableException.class)
    public Dashboard deleteWidgetMappingWithDashboard(
            Integer userId,
            Integer dashboardId,
            Integer widgetId,
            DashboardWidgetMapping dashboardWidgetMapping) {
        Dashboard dashboard = getDashboardById(userId, dashboardId);
        DashboardWidgetMapping existingMapping = getDashboardWidgetMapping(dashboardId, widgetId);
        dashboardWidgetMappingDao.delete(existingMapping);
        dashboard = getDashboardById(userId, dashboardId);
        return dashboard;
    }

}
