package com.proptiger.userservice.exception;

import org.springframework.security.core.AuthenticationException;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.exception.APIException;

/**
 * @author Rajeev Pandey
 *
 */
public class AuthenticationExceptionImpl extends AuthenticationException implements APIException{

    protected String          responseCode     = ResponseCodes.BAD_CREDENTIAL;
    private static final long serialVersionUID = 1L;

    public AuthenticationExceptionImpl(String msg) {
        super(msg);
    }
    
    public AuthenticationExceptionImpl(String code, String msg) {
        super(msg);
        this.responseCode = code;
    }

    @Override
    public String getResponseCode() {
        return responseCode;
    }

    @Override
    public void setResponseCode(String code) {
        this.responseCode = code;
    }

}
