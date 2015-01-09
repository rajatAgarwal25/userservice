package com.proptiger.userservice.config.security.social;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.security.AuthenticationNameUserIdSource;

/**
 * Application's social configiration
 * @author Rajeev Pandey
 *
 */
@Configuration
@EnableSocial
public class AppSocialSecurityConfig implements SocialConfigurer {
    @Autowired
    private DataSource dataSource;

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer cfConfig, Environment env) {
        cfConfig.addConnectionFactory(new FacebookConnectionFactory(env.getProperty("app.fb.appid"), env
                .getProperty("app.fb.secret")));

        /*
         * adding custom google connection factory
         */
        cfConfig.addConnectionFactory(new CustomGoogleConnectionFactory(env.getProperty("app.google.appid"), env
                .getProperty("app.google.secret")));
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        JdbcUsersConnectionRepository repository = new CustomJdbcUsersConnectionRepository(
                dataSource,
                connectionFactoryLocator,
                Encryptors.noOpText(),
                createConnectionSignUp());
        repository.setConnectionSignUp(createConnectionSignUp());
        return repository;
    }

    @Bean
    public ConnectionSignUpImpl createConnectionSignUp() {
        return new ConnectionSignUpImpl();
    }

    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    @Bean
    public SocialUserDetailServiceImpl socialUserDetailsService() {
        return new SocialUserDetailServiceImpl();
    }
}
