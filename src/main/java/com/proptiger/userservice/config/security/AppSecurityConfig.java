package com.proptiger.userservice.config.security;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.CustomRedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.DatabaseSessionOperations;
import org.springframework.session.data.redis.RedisAndDBOperationsSessionRepository;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.filter.GenericFilterBean;

import com.proptiger.api.filter.IPBasedAPIAccessFilter;
import com.proptiger.core.config.CustomAccessDeniedHandler;
import com.proptiger.core.enums.security.UserRole;
import com.proptiger.core.util.Constants;
import com.proptiger.core.util.PropertyKeys;
import com.proptiger.core.util.PropertyReader;
import com.proptiger.userservice.config.security.social.CustomSpringSocialConfigurer;
import com.proptiger.userservice.service.OTPService;

/**
 * Application security configurations. Define url regex and handlers to handle
 * success and failure scenarios. Define filters to process the requests.
 * 
 * @author Rajeev Pandey
 * 
 */
@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = { "com.proptiger" })
@Order
public class AppSecurityConfig<S extends ExpiringSession> extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailManagerService userService;

    @Autowired
    private DataSource               dataSource;

    @Autowired
    private SessionRepository<S>     sessionRepository;

    @Autowired
    private RedisSerializer<?>       jdkSerializer;

    private final int                REDIS_DB_INDEX_SESSION = 1;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(createSessionRepositoryFilter(), ChannelProcessingFilter.class);

        http.rememberMe().rememberMeServices(createPersistentTokenBasedRememberMeService())
                .key(Constants.Security.REMEMBER_ME_COOKIE);
        http.csrf().disable();

        http.authorizeRequests()
                .regexMatchers(".*"+Constants.Security.USER_ROLE_API_REGEX).access("hasRole('" + UserRole.Admin.name() + "')")
                .regexMatchers(".*"+Constants.Security.USER_DETAIL_API_REGEX).access("hasRole('" + UserRole.Admin.name() + "')")
                .regexMatchers(".*"+Constants.Security.USER_API_REGEX).access("hasRole('" + UserRole.USER.name() + "')")
                .regexMatchers(".*"+Constants.Security.USER_API_REGEX).access("hasRole('" + UserRole.Admin.name() + "')")
                .regexMatchers(".*"+Constants.Security.OTP_VALIDATE_API_REGEX).access("hasRole('" + UserRole.PRE_AUTH_USER.name() + "')")
                .regexMatchers(".*"+Constants.Security.SUDO_USER_API_REGEX).access("hasRole('" + UserRole.Admin.name() + "')")
                .anyRequest().permitAll();

        http.exceptionHandling().authenticationEntryPoint(createAuthEntryPoint());
        http.addFilter(createUserNamePasswordLoginFilter());
        http.addFilter(createRememberMeAuthFilter());
        http.logout().logoutSuccessHandler(createLogoutHanlder()).logoutUrl(Constants.Security.LOGOUT_URL)
                .addLogoutHandler(createLogoutHandler());

        http.apply(createSocialAuthConfigurer());
        http.addFilter(createConcurrentSessionFilter());
        http.exceptionHandling().accessDeniedHandler(createAccessDeniedHandler());
        http.addFilterBefore(createIPBasedAPIAccessFilter(), CustomUsernamePasswordAuthenticationFilter.class);
        http.addFilter(createSwichUserFilter());
    }

    @Bean
    public Filter createSwichUserFilter() {
        SwitchUserFilter switchUserFilter = new CustomSwitchUserFilter();  
        switchUserFilter.setUsernameParameter(Constants.Security.SWITCH_USER_PARAMETER_NAME);
        switchUserFilter.setSwitchUserUrl(Constants.Security.SUDO_USER_URL);
        switchUserFilter.setExitUserUrl(Constants.Security.EXIT_SUDO_USER_URL);
        switchUserFilter.setUserDetailsService(userService);
        switchUserFilter.setSuccessHandler(createAuthSuccessHandler());
        switchUserFilter.setFailureHandler(createAuthFailureHandler());
        return switchUserFilter;
    }

    /**
     * Create session repository filter, that will create session object instead
     * of container
     * 
     * @return
     */
    @Bean(name = "springSessionRepositoryFilter")
    public SessionRepositoryFilter<? extends ExpiringSession> createSessionRepositoryFilter() {
        final SessionRepositoryFilter<S> sessionRepositoryFilter = new SessionRepositoryFilter<S>(sessionRepository);
        CookieHttpSessionStrategy httpSessionStrategy = new CookieHttpSessionStrategy();
        httpSessionStrategy.setCookieName(Constants.JSESSIONID);
        sessionRepositoryFilter.setHttpSessionStrategy(httpSessionStrategy);
        return sessionRepositoryFilter;
    }

    @Bean
    public Filter createIPBasedAPIAccessFilter() {
        return new IPBasedAPIAccessFilter();
    }

    @Bean
    public AccessDeniedHandler createAccessDeniedHandler() {
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();
        return accessDeniedHandler;
    }

    @Bean
    public LogoutHandler createLogoutHandler() {
        return new CookieClearingLogoutHandler(
                Constants.Security.COOKIE_NAME_JSESSIONID,
                Constants.Security.REMEMBER_ME_COOKIE);
    }

    @Bean
    public CustomSpringSocialConfigurer createSocialAuthConfigurer() {
        return new CustomSpringSocialConfigurer();
    }

    @Bean
    public LogoutSuccessHandler createLogoutHanlder() {
        return new ApiLogoutSuccessHandler();
    }

    @Bean
    public LoginUrlAuthenticationEntryPoint createAuthEntryPoint() {
        AuthEntryPoint authEntryPoint = new AuthEntryPoint(Constants.Security.LOGIN_URL);
        return authEntryPoint;
    }

    @Bean
    public AuthSuccessHandler createAuthSuccessHandler() {
        return new AuthSuccessHandler();
    }

    @Bean
    public GenericFilterBean createUserNamePasswordLoginFilter() throws Exception {
        UsernamePasswordAuthenticationFilter loginFilter = new CustomUsernamePasswordAuthenticationFilter(
                createOTPService());
        loginFilter.setSessionAuthenticationStrategy(createSessionStrategy());
        loginFilter.setAuthenticationManager(createAuthenticationManager());
        loginFilter.setAuthenticationFailureHandler(createAuthFailureHandler());
        loginFilter.setAuthenticationSuccessHandler(createAuthSuccessHandler());
        loginFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(
                Constants.Security.LOGIN_URL,
                "POST"));
        loginFilter.setUsernameParameter(Constants.Security.USERNAME_PARAMETER_NAME);
        loginFilter.setPasswordParameter(Constants.Security.PASSWORD_PARAMETER_NAME);
        loginFilter.setPostOnly(true);

        loginFilter.setRememberMeServices(createPersistentTokenBasedRememberMeService());
        return loginFilter;
    }

    @Bean
    public OTPService createOTPService() {
        return new OTPService();
    }

    @Bean
    public SessionAuthenticationStrategy createSessionStrategy() {
        List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>();
        delegateStrategies.add(createConcurrentSessionControlAuthenticationStrategy());
        delegateStrategies.add(new RegisterSessionAuthenticationStrategy(createSessionRegistry()));
        CompositeSessionAuthenticationStrategy sessionControlStrategy = new CompositeSessionAuthenticationStrategy(
                delegateStrategies);
        return sessionControlStrategy;
    }

    @Bean
    public ConcurrentSessionControlAuthenticationStrategy createConcurrentSessionControlAuthenticationStrategy() {
        ConcurrentSessionControlAuthenticationStrategy authenticationStrategy = new CustomConcurrentSessionControlAuthenticationStrategy(
                createSessionRegistry());
        // -1 meaning unlimited session allowed for a user
        authenticationStrategy.setMaximumSessions(-1);
        return authenticationStrategy;
    }

    @Bean
    public Filter createConcurrentSessionFilter() {
        SessionRegistry sessionRegistry = createSessionRegistry();
        ConcurrentSessionFilter cf = new ConcurrentSessionFilter(sessionRegistry, Constants.Security.LOGIN_URL);
        cf.setLogoutHandlers(new LogoutHandler[] { createLogoutHandler() });
        cf.setRedirectStrategy(new SessionExpiryStrategyHandler());
        return cf;
    }

    @Bean
    public SessionRegistry createSessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AuthenticationManager createAuthenticationManager() {
        List<AuthenticationProvider> authProvider = new ArrayList<>();
        DaoAuthenticationProvider daoAuthProvider = createDaoAuthProvider();
        authProvider.add(daoAuthProvider);

        authProvider.add(createRememberMeAuthProvider());
        return new ProviderManager(authProvider);
    }
    @Bean
    public DaoAuthenticationProvider createDaoAuthProvider() {
        DaoAuthenticationProvider daoAuthProvider = new DaoAuthenticationProvider();
        daoAuthProvider.setPostAuthenticationChecks(createPostAuthCheckProvider());
        daoAuthProvider.setUserDetailsService(userService);
        daoAuthProvider.setPasswordEncoder(createPasswordEncoder());
        return daoAuthProvider;
    }

    @Bean
    public UserDetailsChecker createPostAuthCheckProvider() {
        UserDetailsChecker userDetailsChecker = new PostAuthenticationCheck();
        return userDetailsChecker;
    }

    @Bean
    public Md5PasswordEncoder createPasswordEncoder() {
        return new Md5PasswordEncoder();
    }

    @Bean
    public RememberMeAuthenticationProvider createRememberMeAuthProvider() {
        return new RememberMeAuthenticationProvider(Constants.Security.API_SECRET_KEY);
    }

    @Bean
    public GenericFilterBean createRememberMeAuthFilter() {
        CustomRememberMeAuthFilter rememberMeFilter = new CustomRememberMeAuthFilter(
                createAuthenticationManager(),
                createPersistentTokenBasedRememberMeService());
        rememberMeFilter.setSessionStrategy(createSessionStrategy());
        return rememberMeFilter;
    }

    @Bean
    public PersistentTokenBasedRememberMeServices createPersistentTokenBasedRememberMeService() {
        PersistentTokenBasedRememberMeServices tokenBasedRememberMeService = new CustomPersistentTokenBasedRememberMeServices(
                Constants.Security.API_SECRET_KEY,
                userService,
                createPersistentLoginRepository());
        tokenBasedRememberMeService.setParameter(Constants.Security.REMEMBER_ME_PARAMETER);
        tokenBasedRememberMeService.setCookieName(Constants.Security.REMEMBER_ME_COOKIE);
        tokenBasedRememberMeService.setTokenValiditySeconds(Constants.Security.REMEMBER_ME_COOKIE_VALIDITY);
        return tokenBasedRememberMeService;
    }

    @Bean
    public PersistentTokenRepository createPersistentLoginRepository() {
        CustomJdbcPersistentTokenRepository repo = new CustomJdbcPersistentTokenRepository();
        return repo;
    }

    @Bean
    public AuthenticationFailureHandler createAuthFailureHandler() {
        AuthFailureHandler authFailureHandler = new AuthFailureHandler();
        return authFailureHandler;
    }

    /**
     * Create an instance of the {@link RedisAndDBOperationsSessionRepository}.
     * Set
     * {@link RedisAndDBOperationsSessionRepository#setDefaultMaxInactiveInterval(int)}
     * to {@link 7 days}.
     * 
     * @param redisTemplate
     * @return A RedisAndDBOperationsSessionRepository instance
     */
    @Bean
    public RedisAndDBOperationsSessionRepository createSessionRepository() {

        RedisAndDBOperationsSessionRepository redisAndDBOperationsSessionRepository = new RedisAndDBOperationsSessionRepository(
                createSessionRedisTemplate(),
                createExpirationSessionRedisTemplate(),
                createDatabaseSessionOperations());

        redisAndDBOperationsSessionRepository.setDefaultMaxInactiveInterval(PropertyReader.getRequiredPropertyAsType(
                PropertyKeys.SESSION_MAX_INTERACTIVE_INTERVAL,
                Integer.class));

        return redisAndDBOperationsSessionRepository;
    }

    @Bean
    public DatabaseSessionOperations createDatabaseSessionOperations() {
        CustomRedisCacheManager cacheManager = new CustomRedisCacheManager(createSessionRedisTemplate());
        cacheManager.setUsePrefix(true);
        DatabaseSessionOperations sessionOperations = new DatabaseSessionOperations(cacheManager);
        return sessionOperations;
    }

    @Bean(name = "sessionRedisTemplate")
    public RedisTemplate<String, ExpiringSession> createSessionRedisTemplate() {
        RedisTemplate<String, ExpiringSession> redisTemplate = new RedisTemplate<String, ExpiringSession>();
        redisTemplate.setConnectionFactory(createSessionJedisConnectionFactory());
        redisTemplate.setValueSerializer(jdkSerializer);
        return redisTemplate;
    }

    @Bean(name = "expirationSessionRedisTemplate")
    public RedisTemplate<String, String> createExpirationSessionRedisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(createSessionJedisConnectionFactory());
       // redisTemplate.setValueSerializer(jdkSerializer);
        return redisTemplate;
    }

    @Bean(name = "sessionJedisConnectionFactory")
    public RedisConnectionFactory createSessionJedisConnectionFactory() {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(PropertyReader.getRequiredPropertyAsType(PropertyKeys.REDIS_HOST, String.class));
        connectionFactory.setPort(PropertyReader.getRequiredPropertyAsType(PropertyKeys.REDIS_PORT, Integer.class));
        connectionFactory.setUsePool(PropertyReader.getRequiredPropertyAsType(
                PropertyKeys.REDIS_USE_POOL,
                Boolean.class));
        connectionFactory.setDatabase(REDIS_DB_INDEX_SESSION);
        return connectionFactory;
    }
}
