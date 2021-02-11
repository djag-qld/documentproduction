package au.gov.qld.bdm.documentproduction.document;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.ServletTestExecutionListener;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.template.TemplateService;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {SpringBootDependencyInjectionTestExecutionListener.class, ServletTestExecutionListener.class})
public class DocumentServiceTest {
	
	@Autowired DocumentService service;
	@Autowired DocumentSignatureService signatureService;
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
		
		
		String signatureKeyAlias = RandomStringUtils.randomAlphabetic(10);
		String kmsId = RandomStringUtils.randomAlphabetic(10);
		String certificate = RandomStringUtils.randomAlphabetic(10);
		String timestampEndpoint = RandomStringUtils.randomAlphabetic(10);
		signatureKeyService.save(credential, signatureKeyAlias, kmsId, certificate, timestampEndpoint );
		
		String signatureAlias = RandomStringUtils.randomAlphabetic(10);
		signatureService.save(credential, signatureAlias, signatureKeyAlias, 1, "reason template", "signatory template", "location template", "contact info template");
		
		String templateAlias = RandomStringUtils.randomAlphabetic(10);
		String content = "<html>test ${templateModel['vara']}</html>";
		templateService.save(credential, templateAlias, content);
		
		Map<String, String> templateModel = new HashMap<>();
		templateModel.put("vara", "a");
		String documentId = service.record(credential, templateAlias, asList(signatureAlias));
		
		DataTablesInput input = new DataTablesInput();
		input.setLength(10);
		input.setStart(0);
		assertThat(service.list(input, "test agency").getData().iterator().next().getTemplate().getAlias(), is(templateAlias));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		service.produce(credential, documentId, templateModel, DocumentOutputFormat.PDF, os);
		assertThat(os.toByteArray().length, greaterThan(0));
	}
}

