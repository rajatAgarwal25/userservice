package com.proptiger.userservice.config.security.social;

import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;

/**
 * Changing default implementation of GoogleConnectionFactory that returns email
 * id as provider user id from AccessGrant.So this implementation returns null
 * that indicates, remote API should be used to get provider user id
 * 
 * 
 * @author Rajeev Pandey
 *
 */
public class CustomGoogleConnectionFactory extends GoogleConnectionFactory {

    public CustomGoogleConnectionFactory(String clientId, String clientSecret) {
        super(clientId, clientSecret);
    }

    // TODO in further release of spring social google this may be changed to
    // return provider user id from this method itself, so that should be used and this
    // class should be deprecated to save api hit
    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        return null;
    }
}
