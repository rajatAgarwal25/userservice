package com.proptiger.userservice.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.proptiger.userservice.util.UserServiceConstants;

/**
 * @author Rajeev Pandey
 *
 */
@Service
public class ExternalAPICallService {

    private static Logger   logger = LoggerFactory.getLogger(ExternalAPICallService.class);
    @Autowired
    private HttpRequestUtil httpRequestUtil;

    public List<Locality> getLocalities(FIQLSelector selector) {
        return httpRequestUtil.getInternalApiResultAsTypeList(
                getCompleteURIUsingSelector(
                        PropertyReader.getRequiredPropertyAsString(UserServiceConstants.URL_LOCALITY_V4),
                        selector),
                Locality.class);
    }

    public List<Dashboard> getDashboardOfActiveUser() {
        String stringUrl = PropertyReader.getRequiredPropertyAsString(PropertyKeys.PROPTIGER_URL) + PropertyReader
                .getRequiredPropertyAsString(UserServiceConstants.URL_DASHBOARD);
        HttpHeaders requestHeaders = createJsessionIdHeader();
        try {
            return httpRequestUtil.getInternalApiResultAsTypeList(
                    URI.create(stringUrl),
                    requestHeaders,
                    Dashboard.class);
        }
        catch (Exception e) {
            logger.error("Exception while getting dashboard of user ", e);
            ;
        }
        return new ArrayList<Dashboard>();
    }

    public List<Trend> getTrend(FIQLSelector selector) {
        return httpRequestUtil.getInternalApiResultAsTypeList(
                getCompleteURIUsingSelector(
                        PropertyReader.getRequiredPropertyAsString(UserServiceConstants.URL_TREND_V1),
                        selector),
                Trend.class);

    }

    public String getB2bAttributeByName(String attributeName) {
        String stringUrl = PropertyReader.getRequiredPropertyAsString(PropertyKeys.PROPTIGER_URL) + PropertyReader
                .getRequiredPropertyAsString(UserServiceConstants.URL_B2B_ATTRIBUTE) + attributeName;
        return httpRequestUtil.getInternalApiResultAsTypeFromCache(URI.create(stringUrl), String.class);
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
