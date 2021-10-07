package au.gov.qld.bdm.documentproduction.document;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.zip.Inflater;

import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.operator.ContentSigner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.document.entity.Document;
import au.gov.qld.bdm.documentproduction.sign.ContentSignerFactory;
import au.gov.qld.bdm.documentproduction.sign.StubContentSigner;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;
import nl.minvws.encoding.Base45;

@RunWith(MockitoJUnitRunner.class)
public class SignedQRCodeServiceTest {
	SignedQRCodeService signedQRCodeService;
	
	@Mock SignatureRecordService signatureRecordService;
	@Mock ContentSignerFactory contentSignerFactory;
	@Mock SignatureKeyService signatureKeyService;
	@Mock SignatureKey signatureKey;
	@Mock Document document;

	ContentSigner contentSigner;
	String keyAlias;
	Map<String, String> templateModel;
	AuditableCredential credential;

	@Before
	public void setUp() {
		contentSigner = new StubContentSigner();
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
		templateModel = ImmutableMap.of("var", "value");
		keyAlias = RandomStringUtils.randomAlphabetic(10);
		when(signatureKey.getKmsId()).thenReturn(RandomStringUtils.randomAlphabetic(10));
		when(contentSignerFactory.create(signatureKey)).thenReturn(contentSigner);
		signedQRCodeService = new SignedQRCodeService(signatureKeyService, contentSignerFactory, signatureRecordService);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenAddingSignatureThatDoesNotExistForAgency() {
		when(signatureKeyService.findKeyForAlias(credential.getAgency(), keyAlias)).thenReturn(Optional.empty());
		signedQRCodeService.create(credential, document, keyAlias, templateModel);
	}
	
	@Test
	public void shouldCreateEncodedAndCompressedAndSignedQRContent() throws Exception {
		when(signatureKeyService.findKeyForAlias(credential.getAgency(), keyAlias)).thenReturn(Optional.of(signatureKey));
		String qrContent = signedQRCodeService.create(credential, document, keyAlias, templateModel);
		
		byte[] decoded = Base45.getDecoder().decode(qrContent);
		String inflated = inflate(decoded);
		SignedQRContent content = new Gson().fromJson(inflated, SignedQRContent.class); 
		assertThat(content.getF(), is(templateModel));
		
		byte[] decodedSignature = Base45.getDecoder().decode(content.getSig());
		assertThat(decodedSignature, is(contentSigner.getSignature()));
		
		verify(signatureRecordService).storeSignature(contentSigner.getSignature(), contentSigner.getAlgorithmIdentifier().getAlgorithm().getId(), 
				signatureKey.getKmsId(), credential.getAgency());
	}
	
	// Effectively there is nothing to sign against when there's no template data. 
	// This also prevents recording signatures when doing document previews. 
	@Test
	public void shouldCreateEncodedAndCompressedAndQRContentWithoutSignatureWhenTemplateModelEmpty() throws Exception {
		templateModel = Collections.emptyMap();
		String qrContent = signedQRCodeService.create(credential, document, keyAlias, templateModel);
		
		byte[] decoded = Base45.getDecoder().decode(qrContent);
		String inflated = inflate(decoded);
		SignedQRContent content = new Gson().fromJson(inflated, SignedQRContent.class); 
		assertThat(content.getF(), is(templateModel));
		
		assertThat(content.getSig(), nullValue());
		verifyNoInteractions(signatureRecordService, signatureKeyService);
	}

	private String inflate(byte[] decoded) throws Exception {
		Inflater decompresser = new Inflater();
		decompresser.setInput(decoded, 0, decoded.length);
		byte[] result = new byte[1024];
		int resultLength = decompresser.inflate(result);
		decompresser.end();
		return new String(result, 0, resultLength, "UTF-8");
	}
}
