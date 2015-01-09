package com.proptiger.userservice.config.security.social;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.security.SocialAuthenticationFilter;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.provider.SocialAuthenticationService;
import org.springframework.util.Assert;

import com.proptiger.core.util.Constants;
import com.proptiger.core.util.DateUtil;
import com.proptiger.core.util.PropertyReader;
import com.proptiger.core.util.SecurityContextUtils;
import com.proptiger.userservice.config.security.ModifiableHttpServletRequest;

/**
 * Custom social authentication filter hack request to change for service
 * provider.
 * 
 * @author Rajeev Pandey
 *
 */
public class CustomSocialAuthFilter extends SocialAuthenticationFilter {
    private static Logger                       logger = LoggerFactory.getLogger(CustomSocialAuthFilter.class);
    private static final String                 SCOPE  = "scope";
    private PropertyReader                      propertyReader;

    private UsersConnectionRepository           connectionRepository;

    private CustomJdbcUsersConnectionRepository customJdbcUsersConnectionRepository;

    public CustomSocialAuthFilter(
            PropertyReader propertyReader,
            AuthenticationManager authManager,
            UserIdSource userIdSource,
            UsersConnectionRepository usersConnectionRepository,
            SocialAuthenticationServiceLocator authServiceLocator) {
        super(authManager, userIdSource, usersConnectionRepository, authServiceLocator);
        this.propertyReader = propertyReader;
        this.connectionRepository = usersConnectionRepository;
        if (connectionRepository instanceof CustomJdbcUsersConnectionRepository) {
            customJdbcUsersConnectionRepository = (CustomJdbcUsersConnectionRepository) connectionRepository;
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        /*
         * this code ensures to login in spring social if user have already
         * logged using oauth sdk on mobile device or on website. So once logged
         * in using SDK client will pass the access token and this block of code
         * will validate the same, and make user logged in SecurityContext.
         */
        String accessToken = request.getParameter(Constants.Security.ACCESS_TOKEN);
        if (accessToken != null && !accessToken.isEmpty()) {
            return attemptAuthUsingAccessToken(request, accessToken);
        }

        /*
         * this flow is to make website or other client work even without
         * posting on our app/v1/login/{provider} api. So if authentication
         * created then return it other wise normal flow should execute.
         */
        String provider = request.getParameter("provider");
        String providerUserId = request.getParameter("providerUserId");
        if (provider != null && !provider.isEmpty() && providerUserId != null && !providerUserId.isEmpty()) {
            return attemptAuthUsingProviderAndProviderId(provider, providerUserId, request);
        }
        HttpServletRequest wrappedRequest = addScopeInRequestParameter(request);
        return super.attemptAuthentication(wrappedRequest, response);
    }

    /**
     * Attempt authentication using access_token passed in request
     * 
     * @param request
     * @param accessToken
     * @return
     */
    private Authentication attemptAuthUsingAccessToken(HttpServletRequest request, String accessToken) {
        Set<String> authProviders = getAuthServiceLocator().registeredAuthenticationProviderIds();
        String authProviderId = getProviderId(request);
        if (!authProviders.isEmpty() && authProviderId != null && authProviders.contains(authProviderId)) {
            SocialAuthenticationService<?> authService = getAuthServiceLocator().getAuthenticationService(
                    authProviderId);
            try {
                OAuth2ConnectionFactory<?> factory = (OAuth2ConnectionFactory<?>) authService.getConnectionFactory();
                Connection<?> connection = factory.createConnection(new AccessGrant(accessToken, null, null, DateUtil
                        .addDays(new Date(), Constants.Security.ACCESS_TOKEN_VALIDITY_DAYS).getTime()));
                final SocialAuthenticationToken token = new SocialAuthenticationToken(connection, null);
                Assert.notNull(token.getConnection());
                Authentication auth = SecurityContextUtils.getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                    return authenticateTokenByAuthManager(token);
                }
                return auth;
            }
            catch (Exception e) {
                logger.error("Invalid access token {} {}", accessToken, e);
                throw new AuthenticationServiceException("invalid access token");
            }
        }
        throw new AuthenticationServiceException("could not determine auth service provider");
    }

    private Authentication authenticateTokenByAuthManager(SocialAuthenticationToken token) {
        Authentication success = getAuthenticationManager().authenticate(token);
        Assert.isInstanceOf(SocialUserDetails.class, success.getPrincipal(), "unexpected principle type");
        return success;
    }

    private Authentication attemptAuthUsingProviderAndProviderId(
            String provider,
            String providerUserId,
            HttpServletRequest request) {
        // these string constants are as per defined in checkuser.php
        logger.debug("login attempt using provider and provideruserid {},{}", provider, providerUserId);
        String userName = request.getParameter("userName");
        String email = request.getParameter("email");
        String profileImageUrl = request.getParameter("profileImageUrl");
        if (email != null && !email.isEmpty()) {
            if (customJdbcUsersConnectionRepository != null) {
                return customJdbcUsersConnectionRepository.createAuthenicationByProviderAndProviderUserId(
                        provider,
                        providerUserId,
                        userName,
                        email,
                        profileImageUrl);
            }

        }
        logger.error(
                "invlid data for login using provider and provider id {},{},{},{},{}",
                provider,
                providerUserId,
                userName,
                email,
                profileImageUrl);
        return null;
    }

    /**
     * Adding scope varibale in request parameter as that is mandatory for
     * google auth, for rest like facebook it may be empty
     * 
     * @param request
     * @return
     */
    private HttpServletRequest addScopeInRequestParameter(HttpServletRequest request) {
        String providerId = getProviderId(request);
        String scopeKey = providerId + "." + SCOPE;
        Map<String, String[]> extraParams = new TreeMap<String, String[]>();
        String[] values = { propertyReader.getRequiredProperty(scopeKey) };
        extraParams.put(SCOPE, values);
        HttpServletRequest wrappedRequest = new ModifiableHttpServletRequest(request, extraParams);
        return wrappedRequest;
    }

    /**
     * Default implementation copied from super class.
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("deprecation")
    private String getProviderId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        // uri must start with context path
        uri = uri.substring(request.getContextPath().length());

        // remaining uri must start with filterProcessesUrl
        if (!uri.startsWith(getFilterProcessesUrl())) {
            return null;
        }
        uri = uri.substring(getFilterProcessesUrl().length());

        // expect /filterprocessesurl/provider, not /filterprocessesurlproviderr
        if (uri.startsWith("/")) {
            return uri.substring(1);
        }
        else {
            return null;
        }
    }
}
