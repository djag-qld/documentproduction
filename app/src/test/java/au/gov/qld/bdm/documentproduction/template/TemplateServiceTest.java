package au.gov.qld.bdm.documentproduction.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.ServletTestExecutionListener;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.template.entity.Template;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {SpringBootDependencyInjectionTestExecutionListener.class, ServletTestExecutionListener.class})
public class TemplateServiceTest {
	
	@Autowired private TemplateService service;

	@Test
	public void shouldCrud() {
		AuditableCredential credential = new AuditableCredential() {
			@Override
			public String getId() {
				return "test id";
			}

			@Override
			public String getAgency() {
				return "test agency";
			}
		};

		String templateAlias = RandomStringUtils.randomAlphabetic(10);
		service.save(credential, templateAlias, "some content");
		Template template = service.findByAliasAndAgency(templateAlias, "test agency").get();
		assertThat(template.getAgency(), is("test agency"));
		assertThat(template.getContent(), is("some content"));
		assertThat(template.getVersion(), is(1));
		
		service.save(credential, templateAlias, "some new content");
		template = service.findByAliasAndAgency(templateAlias, "test agency").get();
		assertThat(template.getAgency(), is("test agency"));
		assertThat(template.getContent(), is("some new content"));
		assertThat(template.getVersion(), is(2));
	}
			
}
