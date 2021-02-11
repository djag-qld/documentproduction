package au.gov.qld.bdm.documentproduction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import au.gov.qld.bdm.documentproduction.web.CommonModelInterceptor;

@Configuration
public class AppConfig implements WebMvcConfigurer {
	@Autowired
	private CommonModelInterceptor commonModelInterceptor;
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(commonModelInterceptor);
    }

}
