package au.gov.qld.bdm.documentproduction.document;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
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
	AuditableCredential credential;
	
	@Before
	public void setUp() {
		credential = new AuditableCredential() {
			String id = RandomStringUtils.randomAlphabetic(10);
			String agency = RandomStringUtils.randomAlphabetic(10);
			
			@Override
			public String getId() {
				return id;
			}

			@Override
			public String getAgency() {
				return agency;
			}
		};
	}
	
	@Test
	public void shouldCreateAndListByAgency() {
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
		assertThat(service.list(input, credential.getAgency()).getData().iterator().next().getTemplate().getAlias(), is(templateAlias));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		service.produce(credential, documentId, templateModel, DocumentOutputFormat.PDF, os);
		assertThat(os.toByteArray().length, greaterThan(0));
	}
	
	@Test
	public void shouldCreateDocumentWithSignedQRCode() throws IOException {
		String signatureKeyAlias = RandomStringUtils.randomAlphabetic(10);
		String kmsId = "395a8416-f32b-4613-9401-c487af1f8215";
		String certificate = RandomStringUtils.randomAlphabetic(10);
		String timestampEndpoint = RandomStringUtils.randomAlphabetic(10);
		signatureKeyService.save(credential, signatureKeyAlias, kmsId, certificate, timestampEndpoint);
		
		String signatureAlias = RandomStringUtils.randomAlphabetic(10);
		signatureService.save(credential, signatureAlias, signatureKeyAlias, 1, "reason template", "signatory template", "location template", "contact info template");
		
		String templateAlias = RandomStringUtils.randomAlphabetic(10);
		String content = "<html>test <img src='" + signatureKeyAlias + "' type='signedqrcode' width='250' height='250' /></html>";
		templateService.save(credential, templateAlias, content);
		
		Map<String, String> templateModel = new HashMap<>();
		templateModel.put("vara", "a");
		String documentId = service.record(credential, templateAlias, Collections.emptyList());
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		service.produce(credential, documentId, templateModel, DocumentOutputFormat.PDF, os);
		FileUtils.writeByteArrayToFile(new File(System.getProperty("java.io.tmpdir") + File.separator + "testsignedqr.pdf"), os.toByteArray());
	}
	
	@Test
	public void shouldCreateDocumentWithQRCode() throws IOException {
		String templateAlias = RandomStringUtils.randomAlphabetic(10);
		String content = "<html>test <img src='some data' type='qrcode' width='250' height='250' /></html>";
		templateService.save(credential, templateAlias, content);
		
		Map<String, String> templateModel = new HashMap<>();
		templateModel.put("vara", "a");
		String documentId = service.record(credential, templateAlias, Collections.emptyList());
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		service.produce(credential, documentId, templateModel, DocumentOutputFormat.PDF, os);
		FileUtils.writeByteArrayToFile(new File(System.getProperty("java.io.tmpdir") + File.separator + "testqr.pdf"), os.toByteArray());
	}
}

