package com.proptiger.userservice.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proptiger.core.dto.internal.ActiveUser;
import com.proptiger.core.mvc.BaseController;
import com.proptiger.core.pojo.response.APIResponse;
import com.proptiger.userservice.config.security.APIAccessLevel;
import com.proptiger.userservice.config.security.AccessLevel;
import com.proptiger.userservice.service.UserSessionService;

/**
 * @author Rajeev Pandey
 *
 */
@Controller
public class UserSessionController extends BaseController{

    @Autowired
    private UserSessionService userSessionService;
    
    @RequestMapping(method = RequestMethod.GET, value = "data/v1/entity/session")
    @ResponseBody
    @APIAccessLevel(level = {AccessLevel.INTERNAL_IP, AccessLevel.CALLER_LOGIN, AccessLevel.CALLER_NON_LOGIN})
    public APIResponse getActiveUserSessionBySessionId(HttpServletRequest request,
            HttpServletResponse response){
        ActiveUser activeUser = userSessionService.getActiveSessionOfRequest(request);
        return new APIResponse(activeUser);
    }
}
