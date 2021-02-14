package au.gov.qld.bdm.documentproduction.web;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.TicketValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import au.gov.qld.bdm.documentproduction.Application;

@Component
public class CommonModelInterceptor implements HandlerInterceptor {
	private static final Logger LOG = LoggerFactory.getLogger(CommonModelInterceptor.class);
	private final String fullUrl;
	private final String casServiceLogin;
	private final String appVersion;

	@Autowired
	public CommonModelInterceptor(ServletContext servletContext, @Value("${web.fullUrl}") String fullUrl, @Value("${cas.service.login}") String casServiceLogin) {
		this.fullUrl = fullUrl;
		this.casServiceLogin = casServiceLogin;
		this.appVersion = getAppVersion(servletContext);
	}
	
	private String getAppVersion(ServletContext servletContext) {
		String version = Application.class.getPackage().getImplementationVersion();
		if (isNotBlank(version)) {
			return version;
		}
		
		Properties properties = new Properties();
		try {
			properties.load(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"));
			return defaultString((String)properties.get("Implementation-Version"));
		} catch (NullPointerException | IOException e) {
			return "unknown";
		}
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, TicketValidationException, IllegalAccessException {
		if (request.getRequestURI().startsWith("/public/")) {
			return true;
		}
		
		if (request.getRequestURI().endsWith("/error")) {
			return false;
		}
		
		verifyApiAccess(request);
		
		return true;
	}

	private void verifyApiAccess(HttpServletRequest request) throws IllegalAccessException {
		if (request.getRequestURI().startsWith("/api") && !fullUrl.contains("localhost")) {
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				if ("x-amzn-apigateway-api-id".equals(headerName) && isNotBlank(request.getHeader(headerName))) {
					LOG.info("API request received from: {}", request.getHeader(headerName));
					return;
				}
			}
			
			throw new IllegalAccessException("Access to API not from API Gateway");
		}
	}

	@Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav) throws Exception {
		if (mav == null || request.getRequestURI().startsWith("/error") || request.getRequestURI().startsWith("/public/") || request.getRequestURI().startsWith("/api")) {
            return;
        }
        
		if (request.getUserPrincipal() == null) {
			LOG.warn("No principal in post handle to: {}", request.getRequestURI());
			return;
		}
		
		LOG.info("Admin request processed from: {}", request.getUserPrincipal().getName());
		mav.addObject("fullUrl", fullUrl);
		mav.addObject("casServiceLogin", casServiceLogin);
		
		if (request.getUserPrincipal() instanceof CasAuthenticationToken) {
			AttributePrincipal attributePrincipal = ((CasAuthenticationToken) request.getUserPrincipal()).getAssertion().getPrincipal();
			String agency = RoleHelper.extractAgency(attributePrincipal);
			mav.addObject("username", request.getUserPrincipal().getName());
			mav.addObject("agency", agency);
			mav.addObject("appVersion", appVersion);
		}
    }

}
