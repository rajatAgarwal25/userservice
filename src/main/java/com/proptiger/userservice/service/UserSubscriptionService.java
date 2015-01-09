package com.proptiger.userservice.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.proptiger.core.model.proptiger.SubscriptionPermission;
import com.proptiger.core.model.proptiger.UserSubscriptionMapping;
import com.proptiger.core.repo.UserSubscriptionMappingDao;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.repo.SubscriptionPermissionDao;

@Service
public class UserSubscriptionService {

    @Autowired
    private UserSubscriptionMappingDao userSubscriptionMappingDao;

    @Autowired
    private SubscriptionPermissionDao  subscriptionPermissionDao;

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
}
