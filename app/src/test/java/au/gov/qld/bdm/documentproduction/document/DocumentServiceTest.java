package au.gov.qld.bdm.documentproduction.document;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.ServletTestExecutionListener;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureView;
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
	@Autowired SignatureRecordService signatureRecordService;
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
	public void shouldCreateAndListByAgency() throws IOException {
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
		verifyHasText(PDDocument.load(os.toByteArray()));
	}
	
	@Test
	public void shouldCreateDocumentWithSignedQRCode() throws IOException {
		String signatureKeyAlias = RandomStringUtils.randomAlphabetic(10);
		String kmsId = RandomStringUtils.randomAlphabetic(10);
		String certificate = RandomStringUtils.randomAlphabetic(10);
		String timestampEndpoint = RandomStringUtils.randomAlphabetic(10);
		signatureKeyService.save(credential, signatureKeyAlias, kmsId, certificate, timestampEndpoint);
		
		String signatureAlias = RandomStringUtils.randomAlphabetic(10);
		signatureService.save(credential, signatureAlias, signatureKeyAlias, 1, "reason template", "signatory template", "location template", "contact info template");
		
		String templateAlias = RandomStringUtils.randomAlphabetic(10);
		String template = "<html>test ${templateModel['vara']} <br/><img src='" + signatureKeyAlias + "' type='signedqrcode' width='250' height='250' /></html>";
		templateService.save(credential, templateAlias, template);
		
		Map<String, String> templateModel = new HashMap<>();
		templateModel.put("vara", "a");
		String documentId = service.record(credential, templateAlias, Collections.emptyList());
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		service.produce(credential, documentId, templateModel, DocumentOutputFormat.PDF, os);
		
		FileUtils.writeByteArrayToFile(new File(System.getProperty("java.io.tmpdir") + File.separator + "testsignedqr.pdf"), os.toByteArray());
		verifyHasText(PDDocument.load(os.toByteArray()));
		verifyHasImage(PDDocument.load(os.toByteArray()));
		
		DataTablesInput dataTablesInput = new DataTablesInput();
		dataTablesInput.setLength(1);
		DataTablesOutput<SignatureView> tablesOutput = signatureRecordService.list(dataTablesInput, credential.getAgency());
		SignatureView signatureView = tablesOutput.getData().get(0);
		assertThat(signatureView.getSignatureAlgorithm(), is("2.16.840.1.101.3.4.2.1"));
		assertThat(signatureView.getSignatureHexAlgorithm(), is("SHA-256"));
	}
	
	@Test
	public void shouldCreateDocumentWithQRCode() throws IOException {
		String templateAlias = RandomStringUtils.randomAlphabetic(10);
		String content = "<html>test ${templateModel['vara']} <br/><img src='some data' type='qrcode' width='250' height='250' /></html>";
		templateService.save(credential, templateAlias, content);
		
		Map<String, String> templateModel = new HashMap<>();
		templateModel.put("vara", "a");
		String documentId = service.record(credential, templateAlias, Collections.emptyList());
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		service.produce(credential, documentId, templateModel, DocumentOutputFormat.PDF, os);

		FileUtils.writeByteArrayToFile(new File(System.getProperty("java.io.tmpdir") + File.separator + "testqr.pdf"), os.toByteArray());
		verifyHasText(PDDocument.load(os.toByteArray()));
		verifyHasImage(PDDocument.load(os.toByteArray()));
	}
	
	private List<RenderedImage> getImagesFromPDF(PDDocument document) throws IOException {
		List<RenderedImage> images = new ArrayList<>();
		for (PDPage page : document.getPages()) {
			images.addAll(getImagesFromResources(page.getResources()));
		}

		return images;
	}

	private List<RenderedImage> getImagesFromResources(PDResources resources) throws IOException {
		List<RenderedImage> images = new ArrayList<>();
		for (COSName xObjectName : resources.getXObjectNames()) {
			PDXObject xObject = resources.getXObject(xObjectName);
	
			if (xObject instanceof PDFormXObject) {
				images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
			} else if (xObject instanceof PDImageXObject) {
				images.add(((PDImageXObject) xObject).getImage());
			}
		}
	
		return images;
	}
	
	private void verifyHasImage(PDDocument pdDocument) throws IOException {
		List<RenderedImage> imagesFromPDF = getImagesFromPDF(pdDocument);
		assertThat(imagesFromPDF.size(), is(1));
		assertThat(imagesFromPDF.get(0).getWidth(), is(125));
		assertThat(imagesFromPDF.get(0).getHeight(), is(125));
		pdDocument.close();
	}

	private void verifyHasText(PDDocument pdDocument) throws IOException {
		PDFTextStripper pdfStripper = new PDFTextStripper();
		String pdfContent = pdfStripper.getText(pdDocument);
		assertThat(pdfContent, containsString("test a"));
		pdDocument.close();
	}
}

