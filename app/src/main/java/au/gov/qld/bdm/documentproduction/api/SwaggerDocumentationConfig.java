package au.gov.qld.bdm.documentproduction.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerDocumentationConfig {
	@Value("${api.host}") String apiHost;
	
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Document Production Service")
				.description("").license("").licenseUrl("")
				.termsOfServiceUrl("").version("0.1")
				.contact(new Contact("", "", "")).build();
	}

	@Bean
	public Docket customImplementation() {
		AuthorizationScope[] authScopes = new AuthorizationScope[1];
        authScopes[0] = new AuthorizationScopeBuilder().scope("global").description("full access").build();
        SecurityReference securityReference = SecurityReference.builder().reference("Authorization-Key")
                .scopes(authScopes).build();

        List<SecurityContext> securityContexts = Arrays.asList(
                SecurityContext.builder().securityReferences(Arrays.asList(securityReference)).build());	
		return new Docket(DocumentationType.SWAGGER_2)
				.host(apiHost)
				.securitySchemes(Arrays.asList(new ApiKey("Authorization-Key", "Authorization", "header")))
                .securityContexts(securityContexts)
				.forCodeGeneration(true).select()
				.apis(RequestHandlerSelectors.basePackage("au.gov.qld.bdm.documentproduction.api"))
				.build().tags(new Tag("Signature", "API for applying signatures to documents"), new Tag("Document", "API for producing documents")).apiInfo(apiInfo());
	}

}
