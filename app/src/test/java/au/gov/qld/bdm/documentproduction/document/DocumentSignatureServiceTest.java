package au.gov.qld.bdm.documentproduction.document;
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
import au.gov.qld.bdm.documentproduction.document.entity.DocumentSignature;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.template.TemplateService;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {SpringBootDependencyInjectionTestExecutionListener.class, ServletTestExecutionListener.class})
public class DocumentSignatureServiceTest {
	
	@Autowired DocumentSignatureService service;
	@Autowired SignatureKeyService signatureKeyService;
	@Autowired TemplateService templateService;
	
	@Test
	public void shouldCreateAndListByAgency() {
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
		
		
		String alias = RandomStringUtils.randomAlphabetic(10);
		String signatureKeyAlias = RandomStringUtils.randomAlphabetic(10);
		String kmsId = RandomStringUtils.randomAlphabetic(10);
		String certificate = RandomStringUtils.randomAlphabetic(10);
		String timestampEndpoint = RandomStringUtils.randomAlphabetic(10);
		signatureKeyService.save(credential, signatureKeyAlias, kmsId, certificate, timestampEndpoint);
		
		service.save(credential, alias, signatureKeyAlias, 1, "reason template", "signatory template", "location template", "contact info template");
		DocumentSignature entity = service.findByAliasAndAgency(alias, "test agency").get();
		assertThat(entity.getAgency(), is("test agency"));
		assertThat(entity.getSignatureKey().getKmsId(), is(kmsId));
		assertThat(entity.getVersion(), is(1));
		
		service.save(credential, alias, signatureKeyAlias, 1, "reason template", "signatory template", "location template", "contact info template");
		
		entity = service.findByAliasAndAgency(alias, "test agency").get();
		assertThat(entity.getAgency(), is("test agency"));
		assertThat(entity.getVersion(), is(2));
	}
}

