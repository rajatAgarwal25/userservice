package com.proptiger.userservice.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

import com.proptiger.core.config.WebInitializer;

/**
 * Initialize web application, configuring front controller DispatcherServlet,
 * this file is replacement of web.xml
 * 
 * @author Rajeev Pandey
 */
public class AppWebInitializer extends WebInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onServletStartup(servletContext);
    }
}