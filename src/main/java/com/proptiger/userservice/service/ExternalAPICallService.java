package com.proptiger.userservice.service;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.proptiger.core.model.cms.Locality;
import com.proptiger.core.model.cms.Trend;
import com.proptiger.core.model.proptiger.Dashboard;
import com.proptiger.core.pojo.FIQLSelector;
import com.proptiger.core.util.Constants;
import com.proptiger.core.util.HttpRequestUtil;
import com.proptiger.core.util.PropertyKeys;
import com.proptiger.core.util.PropertyReader;
import com.proptiger.core.util.RequestHolderUtil;

/**
 * @author Rajeev Pandey
 *
 */
@Service
public class ExternalAPICallService {

    private static final String URL_DASHBOARD = "data/v1/entity/user/1/dashboard";
    private static final String URL_LOCALITY_V4 = "data/v4/entity/locality";
    private static final String URL_TREND_V1    = "app/v1/trend";
    private static final String URL_B2B_ATTRIBUTE = "data/v1/entity/b2b/attribute?attributeName=";
    private static final String URL_NOTIFICATION = "data/v1/entity/notification/sender";

    @Autowired
    private HttpRequestUtil     httpRequestUtil;

    public List<Locality> getLocalities(FIQLSelector selector) {
        return httpRequestUtil.getInternalApiResultAsTypeList(
                getCompleteURIUsingSelector(URL_LOCALITY_V4, selector),
                Locality.class);
    }

    public List<Dashboard> getDashboardOfActiveUser() {
        String stringUrl = PropertyReader.getRequiredPropertyAsString(PropertyKeys.PROPTIGER_URL) + URL_DASHBOARD;
        HttpHeaders requestHeaders = createJsessionIdHeader();
        return httpRequestUtil.getInternalApiResultAsTypeList(URI.create(stringUrl), requestHeaders, Dashboard.class);
    }

    public List<Trend> getTrend(FIQLSelector selector) {
        return httpRequestUtil.getInternalApiResultAsTypeList(
                getCompleteURIUsingSelector(URL_TREND_V1, selector),
                Trend.class);

    }

    public String getB2bAttributeByName(String attributeName){
        String stringUrl = PropertyReader.getRequiredPropertyAsString(PropertyKeys.PROPTIGER_URL) + URL_B2B_ATTRIBUTE+attributeName;
        return httpRequestUtil.getInternalApiResultAsTypeFromCache(URI.create(stringUrl), String.class);
    }
    
    public void sendNewUserRegistrationNotification(int userId){
        
    }
    
    private HttpHeaders createJsessionIdHeader() {
        HttpHeaders requestHeaders = new HttpHeaders();
        String jsessionId = RequestHolderUtil.getJsessionIdFromRequestCookie();
        requestHeaders.add("Cookie", Constants.Security.COOKIE_NAME_JSESSIONID + "=" + jsessionId);
        return requestHeaders;
    }

    private URI getCompleteURIUsingSelector(String url, FIQLSelector selector) {
        String stringUrl = PropertyReader.getRequiredPropertyAsString(PropertyKeys.PROPTIGER_URL) + url
                + "?"
                + selector.getStringFIQL();
        return URI.create(stringUrl);
    }
}
