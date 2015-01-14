package com.proptiger.userservice.util;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;

import com.proptiger.core.constants.ResponseCodes;
import com.proptiger.core.constants.ResponseErrorMessages;
import com.proptiger.core.dto.internal.user.RegisterUser;
import com.proptiger.core.exception.BadRequestException;
import com.proptiger.core.model.user.UserContactNumber;
import com.proptiger.core.util.Constants;

/**
 * Util class to validate user registration data
 * 
 * @author Rajeev Pandey
 * 
 */
public class RegistrationUtils {

    /**
     * Validate new user registration data, and encode password
     * 
     * @param register
     */
    public static void validateRegistration(RegisterUser register) {
        /*
         * to make it to work for new website that does not take confirm
         * password field on UI.
         */
        if (register.getConfirmPassword() == null) {
            register.setConfirmPassword(register.getPassword());
        }
        register.setFullName(validateName(register.getFullName()));
        if (!validateEmail(register.getEmail())) {
            throw new BadRequestException(ResponseCodes.BAD_REQUEST, ResponseErrorMessages.User.INVALID_EMAIL);
        }
        String encodedPass = PasswordUtils.validateNewAndConfirmPassword(
                register.getPassword(),
                register.getConfirmPassword());
        register.setPassword(encodedPass);

        if (register.getCountryId() == null) {
            throw new BadRequestException(ResponseCodes.BAD_REQUEST, ResponseErrorMessages.User.INVALID_COUNTRY);
        }
        validateContactNumber(register.getContactNumbers());
    }

    private static void validateContactNumber(Set<UserContactNumber> contacts) {
        // TODO can check minimum and maximum length of phone number as well
        if (contacts != null) {
            Iterator<UserContactNumber> it = contacts.iterator();
            while (it.hasNext()) {
                UserContactNumber contact = it.next();
                if (contact.getContactNumber() == null || contact.getContactNumber().isEmpty()) {
                    throw new BadRequestException(
                            ResponseCodes.BAD_REQUEST,
                            ResponseErrorMessages.User.INVALID_CONTACT_NUMBER);
                }
            }
        }
        else {
            throw new BadRequestException(ResponseCodes.BAD_REQUEST, ResponseErrorMessages.User.INVALID_CONTACT_NUMBER);
        }
    }

    public static boolean validateEmail(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            return false;
        }
        else {
            return true;
        }
    }

    private static String validateName(String username) {
        if(username == null){
            throw new BadRequestException(
                    ResponseCodes.BAD_REQUEST,
                    ResponseErrorMessages.User.USERNAME_LEN_TOO_SHORT);
        }
        username = username.trim();
        if (username.length() < Constants.User.USERNAME_MIN_LEN) {
            throw new BadRequestException(
                    ResponseCodes.BAD_REQUEST,
                    ResponseErrorMessages.User.USERNAME_LEN_TOO_SHORT);
        }
        else if(username.length() > Constants.User.USERNAME_MAX_LEN){
            throw new BadRequestException(
                    ResponseCodes.BAD_REQUEST,
                    ResponseErrorMessages.User.USERNAME_LEN_TOO_LONG);
        }
        return username;
    }

}
