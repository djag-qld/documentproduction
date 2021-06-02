package au.gov.qld.bdm.documentproduction.sign;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.bouncycastle.operator.ContentSigner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.audit.entity.AuditAction;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

@RunWith(MockitoJUnitRunner.class)
public class SigningServiceTest {

	private static final String KEY_ID = "some key";
	SigningService service;
	CertificateResponse certificate;
	
	@Mock ContentSignerFactory contentSignerFactory;
	@Mock ContentSigner contentSigner;
	@Mock AuditService auditService;
	@Mock AuditableCredential credential;
	@Mock SignatureKey signatureKey;
	@Mock SignatureRecordService signatureRecordService;

	@Before
	public void setUp() throws Exception {
		when(contentSignerFactory.create(signatureKey, certificate)).thenReturn(contentSigner);
		when(contentSigner.getOutputStream()).thenReturn(new ByteArrayOutputStream());
		when(contentSigner.getSignature()).thenReturn("test".getBytes());
		when(signatureKey.getKmsId()).thenReturn(KEY_ID);
		service = new SigningService(contentSignerFactory, auditService, signatureRecordService);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	@Ignore // TODO: unable to test signing automatically when using kms. look into a auto generated self sign
	public void shouldSignPdf() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String signatory = "someone";
		String reason = "some reason";
		String location = "some location";
		String contactInfo = "some contact info";
		PDDocument doc = PDDocument.load(getClass().getClassLoader().getResourceAsStream("dummy.pdf")); 
		service.signPdf(doc, signatureKey, signatory, reason, location, contactInfo, credential);
		doc.saveIncremental(os);
		doc.close();
		assertThat(os.toByteArray().length, greaterThan(1)); // TODO: verify signature
		verify(auditService).audit((AuditAction) argThat((ArgumentMatcher) hasProperty("target", equalTo(KEY_ID))));
	}
}
