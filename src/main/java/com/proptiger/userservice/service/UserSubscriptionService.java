package com.proptiger.userservice.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.proptiger.core.enums.DomainObject;
import com.proptiger.core.model.cms.Locality;
import com.proptiger.core.model.cms.Trend;
import com.proptiger.core.model.proptiger.Permission;
import com.proptiger.core.model.proptiger.SubscriptionPermission;
import com.proptiger.core.model.proptiger.UserSubscriptionMapping;
import com.proptiger.core.pojo.FIQLSelector;
import com.proptiger.core.repo.UserSubscriptionMappingDao;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.repo.SubscriptionPermissionDao;

@Service
public class UserSubscriptionService {

    @Autowired
    private ExternalAPICallService externalAPICallService;

    @Autowired
    private UserSubscriptionMappingDao userSubscriptionMappingDao;

    @Autowired
    private SubscriptionPermissionDao  subscriptionPermissionDao;

    public String getUserAppSubscriptionFilters(int userId) {

        List<SubscriptionPermission> subscriptionPermissions = getUserAppSubscriptionDetails(userId);
        List<String> filterList = new ArrayList<String>();

        int objectTypeId = 0;
        Permission permission;
        for (SubscriptionPermission subscriptionPermission : subscriptionPermissions) {

            permission = subscriptionPermission.getPermission();
            objectTypeId = permission.getObjectTypeId();
            switch (DomainObject.getFromObjectTypeId(objectTypeId)) {
                case city:
                    filterList.add("cityId==" + permission.getObjectId());
                    break;
                case locality:
                    filterList.add("localityId==" + permission.getObjectId());
                    break;
                default:
                    break;
            }
        }
        return StringUtils.join(filterList, ",");
    }

    /**
     * @param userId
     *            userId for which subscription permissions are needed.
     * @return List of subscriptionPermissions or an empty-list if there are no
     *         permissions installed.
     */
    @Cacheable(value = Constants.CacheName.CACHE)
    public List<SubscriptionPermission> getUserAppSubscriptionDetails(int userId) {
        List<UserSubscriptionMapping> userSubscriptionMappingList = getUserSubscriptionMappingList(userId);
        if (userSubscriptionMappingList == null) {
            return (new ArrayList<SubscriptionPermission>());
        }

        List<Integer> subscriptionIdList = new ArrayList<Integer>();
        for (UserSubscriptionMapping usm : userSubscriptionMappingList) {
            if (usm.getSubscription().getExpiryTime().getTime() > new Date().getTime()) {
                subscriptionIdList.add(usm.getSubscriptionId());
            }
        }

        if (subscriptionIdList.isEmpty()) {
            return (new ArrayList<SubscriptionPermission>());
        }

        List<SubscriptionPermission> subscriptionPermissions = subscriptionPermissionDao
                .findBySubscriptionIdIn(subscriptionIdList);
        if (subscriptionPermissions == null) {
            return (new ArrayList<SubscriptionPermission>());
        }

        return subscriptionPermissions;
    }

    @Cacheable(value = Constants.CacheName.CACHE)
    public List<UserSubscriptionMapping> getUserSubscriptionMappingList(int userId) {
        return (userSubscriptionMappingDao.findAllByUserId(userId));
    }

    /*
     * Returns a MultiKeyMap with 2 keys. [K1 = ObjectTypeId, K2 = ObjectId],
     * [Value = Permission Object]
     * 
     * [1]. In case of locality, checking existence of the key should be enough.
     * [2]. For city, a key will exist (with a value null), even if the user has
     * a permission for some locality in the city. To check for full city
     * permission, check if the value mapped to that key is not null.
     */

    public MultiKeyMap getUserSubscriptionMap(int userId) {
        List<SubscriptionPermission> subscriptionPermissions = getUserAppSubscriptionDetails(userId);
        MultiKeyMap userSubscriptionMap = new MultiKeyMap();
        Permission permission;
        int objectTypeId, objectId;
        List<Integer> localityIDList = new ArrayList<Integer>();

        int objTypeIdLocality = DomainObject.locality.getObjectTypeId();
        int objTypeIdCity = DomainObject.city.getObjectTypeId();
        for (SubscriptionPermission sp : subscriptionPermissions) {
            permission = sp.getPermission();

            if (permission != null) {
                objectTypeId = permission.getObjectTypeId();
                objectId = permission.getObjectId();
                userSubscriptionMap.put(objectTypeId, objectId, permission);
                if (objectTypeId == objTypeIdLocality) {
                    localityIDList.add(objectId);
                }
            }
        }

        /*
         * populating psuedo permissions for city if any locality in that city
         * is permitted
         */
        Set<Integer> cityIdList = getCityIdListFromLocalityIdList(localityIDList);
        for (int cityId : cityIdList) {
            userSubscriptionMap.put(objTypeIdCity, cityId, null);
        }

        List<Integer> subscribedBuilders = getSubscribedBuilderList(userId);
        for (Integer builderId : subscribedBuilders) {
            userSubscriptionMap.put(DomainObject.builder.getObjectTypeId(), builderId, builderId);
        }
        return userSubscriptionMap;
    }

    @Async
    public void preloadUserSubscriptionMap(int userId) {
        getUserSubscriptionMap(userId);
    }

    @Cacheable(value = Constants.CacheName.CACHE)
    private List<Integer> getSubscribedBuilderList(int userId) {
        FIQLSelector selector = new FIQLSelector().addAndConditionToFilter(getUserAppSubscriptionFilters(userId));
        List<Integer> builderList = new ArrayList<>();
        if (selector.getFilters() != null) {
            String builderId = "builderId";
            selector.setFields(builderId);
            selector.setGroup(builderId);

            List<Trend> list = externalAPICallService.getTrend(selector);
            for (Trend inventoryPriceTrend : list) {
                builderList.add(inventoryPriceTrend.getBuilderId());
            }
        }
        return builderList;
    }

    private Set<Integer> getCityIdListFromLocalityIdList(List<Integer> localityIDList) {
        Set<Integer> cityIdList = new HashSet<Integer>();
        FIQLSelector fiqlSelector = new FIQLSelector();
        for(Integer id: localityIDList){
            fiqlSelector.addOrConditionToFilter("localityId=="+id);
        }
        fiqlSelector.setStart(0).setRows(9999);
        List<Locality> localiltyList = externalAPICallService.getLocalities(fiqlSelector);
        for (Locality locality : localiltyList) {
            cityIdList.add(locality.getSuburb().getCityId());
        }
        return cityIdList;
    }
}
