package com.proptiger.userservice.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.enums.ResourceType;
import com.proptiger.core.enums.ResourceTypeAction;
import com.proptiger.core.enums.UserAttributeTypes;
import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.exception.ResourceNotAvailableException;
import com.proptiger.core.model.user.UserAttribute;
import com.proptiger.userservice.repo.UserAttributeDao;

/**
 * @author Nikhil Singhal
 */

@Service
public class UserAttributeService {

    @Autowired
    private UserAttributeDao          userAttributeDao;

    @Autowired
    private MetaUserAttributesService metaUserAttributeService;

    public UserAttribute createAttribute(ActiveUser activeUser, UserAttribute userAttribute) {

        // List of saved attribute names, used for verification of attribute to
        // be created
        Map<String, Boolean> metaUserAttributes = metaUserAttributeService.getAllMetaAttributes();

        if (metaUserAttributes.get(userAttribute.getAttributeName())) {
            UserAttributeTypes.validate(userAttribute.getAttributeName(), userAttribute.getAttributeValue());
            userAttribute.setUserId(activeUser.getUserIdentifier());
            return userAttributeDao.saveAndFlush(userAttribute);
        }
        else {
            throw new BadRequestException(ResponseCodes.BAD_REQUEST, ResponseErrorMessages.User.INVALID_ATTRIBUTE_NAME);
        }
    }

    public UserAttribute updateAttribute(ActiveUser activeUser, Integer attributeId, String attributeValue) {
        UserAttribute savedUserAttribute = userAttributeDao.findOne(attributeId);
        if (savedUserAttribute == null) {
            throw new ResourceNotAvailableException(ResourceType.USER_ATTRIBUTE, ResourceTypeAction.UPDATE);
        }
        else {
            UserAttributeTypes.validate(savedUserAttribute.getAttributeName(), attributeValue);
            savedUserAttribute.setAttributeValue(attributeValue);
            return userAttributeDao.saveAndFlush(savedUserAttribute);
        }
    }
}
