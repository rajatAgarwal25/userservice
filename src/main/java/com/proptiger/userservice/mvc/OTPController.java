package com.proptiger.userservice.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.core.util.Constants;
import com.proptiger.userservice.service.OTPService;

/**
 * @author Rajeev Pandey
 *
 */
@Controller

public class OTPController extends BaseController{

    @Autowired
    private OTPService otpService;

    @RequestMapping(value = "app/v1/otp/validate", method = RequestMethod.POST)
    public void validateOTP(
            @RequestBody String otp,
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser,
            HttpServletRequest request,
            HttpServletResponse response) {

        otpService.validate(otp, activeUser, request, response);
    }
    
    @RequestMapping(value = "app/v1/otp", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getOTP(
            @ModelAttribute(Constants.LOGIN_INFO_OBJECT_NAME) ActiveUser activeUser) {
        otpService.respondWithOTP(activeUser);;
        return new APIResponse("New OTP has been sent to your registered email");
    }

}
