package com.proptiger.userservice.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proptiger.core.model.user.UserAttribute;

public interface UserAttributeDao extends JpaRepository<UserAttribute, Integer> {
    UserAttribute findByUserIdAndAttributeNameAndAttributeValue(int userId, String attributeName, String attributeValue);

    UserAttribute findByUserIdAndAttributeName(int userId, String attributeName);

    UserAttribute findByUserIdAndAttributeValue(int userId, String attributeValue);

    List<UserAttribute> findByUserId(int id);

}
