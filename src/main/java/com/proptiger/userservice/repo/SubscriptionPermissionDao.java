package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proptiger.core.model.proptiger.SubscriptionPermission;

public interface SubscriptionPermissionDao extends JpaRepository<SubscriptionPermission, Integer> {

    public List<SubscriptionPermission> findBySubscriptionIdIn(List<Integer> subscriptionIdList);
}