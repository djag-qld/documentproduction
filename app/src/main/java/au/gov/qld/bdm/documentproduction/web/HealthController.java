package au.gov.qld.bdm.documentproduction.web;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import au.gov.qld.bdm.documentproduction.Application;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;

@RestController
public class HealthController {
	private static final Logger LOG = LoggerFactory.getLogger(HealthController.class);
	
	private final String appVersion;
	private final SignatureKeyService keyService;

	@Autowired
	public HealthController(ServletContext servletContext, SignatureKeyService keyService) {
		this.keyService = keyService;
		this.appVersion = getAppVersion(servletContext);
	}
	
	@GetMapping("/health/check")
	public HealthCheckResponse check() {
		LOG.debug("Checking health");
		keyService.checkHealth();
		return new HealthCheckResponse().withVersion(appVersion).okay();
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
}
