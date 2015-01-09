package com.proptiger.userservice.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.exception.ProAPIException;
import com.proptiger.core.exception.ResourceAlreadyExistException;
import com.proptiger.core.exception.UnauthorizedException;
import com.proptiger.core.model.user.UserPreference;
import com.proptiger.core.util.ExclusionAwareBeanUtilsBean;
import com.proptiger.userservice.repo.UserPreferenceDao;
import com.proptiger.userservice.util.UserPreferenceProcessor;

/**
 * B2b User Detail Service Class
 * 
 * @author Azitabh Ajit
 * 
 */

@Service
public class UserPreferenceService {
    @Autowired
    private UserPreferenceDao userPreferenceDao;

    public UserPreference createUserPreference(UserPreference preference, int userId) {
        if (userPreferenceDao.findByUserIdAndApp(userId, preference.getApp()) != null) {
            throw new ResourceAlreadyExistException("Preference Already Created For the app " + preference.getApp());
        }
        if (!UserPreferenceProcessor.isValidPreference(preference.getPreference())) {
            throw new BadRequestException(ResponseErrorMessages.User.INVALID_USER_PREFERENCE);
        }

        preference.setCreatedAt(new Date());
        preference.setUserId(userId);
        preference.setId(null);

        userPreferenceDao.save(preference);
        return preference;
    }

    public UserPreference updateUserPreference(UserPreference preference, int UserId) {
        UserPreference pastPreference = userPreferenceDao.findOne(preference.getId());

        if (pastPreference.getUserId() != UserId) {
            throw new UnauthorizedException();
        }
        if (!UserPreferenceProcessor.isValidPreference(preference.getPreference()) || !pastPreference.getApp().equals(
                preference.getApp())) {
            throw new BadRequestException(ResponseErrorMessages.User.INVALID_USER_PREFERENCE);
        }

        ExclusionAwareBeanUtilsBean beanUtilsBean = new ExclusionAwareBeanUtilsBean();
        try {
            beanUtilsBean.copyProperties(pastPreference, preference);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new ProAPIException(e);
        }

        pastPreference.setUpdatedAt(new Date());
        userPreferenceDao.save(pastPreference);
        return pastPreference;
    }

    public List<UserPreference> getUserPreferences(int userId) {
        List<UserPreference> preferences = userPreferenceDao.findByuserId(userId);
        for (UserPreference preference : preferences) {
            preference.setPreference(UserPreferenceProcessor.mergeDefaultPreference(preference.getPreference()));
        }
        return preferences;
    }
}