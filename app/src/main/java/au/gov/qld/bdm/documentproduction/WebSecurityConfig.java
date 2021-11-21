package au.gov.qld.bdm.documentproduction;

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private final String casServiceLogin;
	private final String casServiceLogout;
	private final String casTicketUrlPrefix;
	private final String appServiceHome;
	private final String serverName;
	private final boolean secure;
	private final UserService userService;

	@Autowired
	public WebSecurityConfig(UserService userService, @Value("${web.servername}") String serverName, @Value("${web.secure}") boolean secure,
			@Value("${cas.service.login}") String casServiceLogin, @Value("${cas.service.logout}") String casServiceLogout,
			@Value("${cas.ticket.url.prefix}") String casTicketUrlPrefix, @Value("${app.service.home}") String appServiceHome) {
		super(true);
		this.userService = userService;
		this.serverName = serverName;
		this.casServiceLogin = casServiceLogin;
		this.casServiceLogout = casServiceLogout;
		this.casTicketUrlPrefix = casTicketUrlPrefix;
		this.appServiceHome = appServiceHome;
		this.secure = secure;
	}
	
	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties sp = new ServiceProperties();
		sp.setService(appServiceHome);
		sp.setSendRenew(false);
		return sp;
	}

	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(customUserDetailsService());
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
		casAuthenticationProvider.setKey(serverName);
		return casAuthenticationProvider;
	}

	@Bean
	public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> customUserDetailsService() {
		return userService;
	}

	@Bean
	public SessionAuthenticationStrategy sessionStrategy() {
		SessionAuthenticationStrategy sessionStrategy = new SessionFixationProtectionStrategy();
		return sessionStrategy;
	}

	@Bean
	public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
		return new Cas20ServiceTicketValidator(casTicketUrlPrefix);
	}

	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		casAuthenticationFilter.setSessionAuthenticationStrategy(sessionStrategy());
		casAuthenticationFilter.setRequiresAuthenticationRequestMatcher(request -> {
			return request.getParameter("ticket") != null;
		});
		return casAuthenticationFilter;
	}

	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		casAuthenticationEntryPoint.setLoginUrl(casServiceLogin);
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
		return casAuthenticationEntryPoint;
	}

	@Bean
	public LogoutFilter requestCasGlobalLogoutFilter() {
		LogoutFilter logoutFilter = new LogoutFilter(casServiceLogout + "?service=" + appServiceHome, new SecurityContextLogoutHandler());
		logoutFilter.setFilterProcessesUrl("logout");
		logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher("logout"));
		return logoutFilter;
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/public/**/*", "/api/**/*", "/health/check");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authenticationProvider(casAuthenticationProvider());
		http
			.csrf().and()
			.headers().and()
			.securityContext().and()
			.servletApi().and()
			.exceptionHandling()
				.authenticationEntryPoint(casAuthenticationEntryPoint()).and().addFilter(casAuthenticationFilter())
				.addFilterBefore(requestCasGlobalLogoutFilter(), LogoutFilter.class);
		if (secure) {
			http.requiresChannel().anyRequest().requiresSecure();
		}
		
		http.authorizeRequests().antMatchers("/user/**").fullyAuthenticated();
		http.logout().logoutSuccessUrl(casServiceLogout).invalidateHttpSession(true).deleteCookies("JSESSIONID");
	}

}