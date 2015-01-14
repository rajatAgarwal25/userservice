package com.proptiger.userservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proptiger.core.model.user.MetaUserAttributes;

/**
 * @author Nikhil Singhal
 */

@Service
public class MetaUserAttributesService {

    @Autowired
    private MetaUserAttributesDao metaUserAttributesDao;

    public Map<String, Boolean> getAllMetaAttributes() {

        Map<String, Boolean> attributeNamesMap = new HashMap<String, Boolean>();

        List<MetaUserAttributes> metaUserAttributes = metaUserAttributesDao.findAll();

        for (MetaUserAttributes metaUserAttribute : metaUserAttributes) {
            attributeNamesMap.put(metaUserAttribute.getAttributeName(), true);
        }

        return attributeNamesMap;
    }
}
