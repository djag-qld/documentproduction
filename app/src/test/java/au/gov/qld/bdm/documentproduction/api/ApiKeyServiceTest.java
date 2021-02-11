package au.gov.qld.bdm.documentproduction.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiKeyServiceTest {
	@Autowired ApiKeyService service;
	
	@Test
	public void shouldInsertAndVerify() {
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
		
		service.save(credential, "some.apikey");
		assertThat(service.list("test agency").iterator().next().getApiKeyId(), is("some"));
		assertThat(service.list("test agency").iterator().next().isEnabled(), is(true));
		assertThat(service.list("other").size(), is(0));
		
		assertThat(service.validate("some.apikey").isPresent(), is(true));
		assertThat(service.validate("some.apikeywrong").isPresent(), is(false));
		
		service.toggleEnabled(credential, "some");
		assertThat(service.list("test agency").iterator().next().isEnabled(), is(false));
		assertThat(service.validate("some.apikey").isPresent(), is(false));
	}
}
