package au.gov.qld.bdm.documentproduction.sign;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

@RunWith(MockitoJUnitRunner.class)
public class SigningServiceTest {
	private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
	private static final String END_CERT = "-----END CERTIFICATE-----";
	static ContentSigner contentSigner;
	static X509Certificate certificate;
	
	SigningService service;
	@Mock ContentSignerFactory contentSignerFactory;
	@Mock AuditService auditService;
	@Mock SignatureRecordService signatureRecordService;
	@Mock AuditableCredential credential;
	@Mock SignatureKey signatureKey;
	
	PDDocument doc;
	String signatory;
	String reason;
	String location;
	String contactInfo;
	
	@BeforeClass
	public static void setUpProvider() throws Exception {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		setUpKeys();
	}
	
	@Before
	public void setUp() throws Exception {
        when(signatureKey.getCertificate()).thenReturn(convertToPem(certificate));
        when(contentSignerFactory.create(eq(signatureKey), isA(CertificateResponse.class))).thenReturn(contentSigner);
        
		signatory = RandomStringUtils.randomAlphanumeric(10);
		reason = RandomStringUtils.randomAlphanumeric(10);
		location = RandomStringUtils.randomAlphanumeric(10);
		contactInfo = RandomStringUtils.randomAlphanumeric(10);
		doc = PDDocument.load(getClass().getClassLoader().getResourceAsStream("dummy.pdf"));
        
		service = new SigningService(contentSignerFactory, auditService, signatureRecordService);
	}
	
	@Test
	public void shouldSignContent() throws Exception {
		service.signPdf(doc, signatureKey, signatory, reason, location, contactInfo, credential);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.saveIncremental(baos);
		doc.close();
		baos.close();
		byte[] signedPdfData = baos.toByteArray();
		PDDocument signed = PDDocument.load(signedPdfData);
		PDSignature signature = signed.getSignatureDictionaries().get(0);
		assertThat(signature.getReason(), is(reason));
		assertThat(signature.getContactInfo(), is(contactInfo));
		assertThat(signature.getLocation(), is(location));
		assertThat(signature.getName(), is(signatory));
		
		byte[] signatureAsBytes = signature.getContents(signedPdfData);
        byte[] signedContentAsBytes = signature.getSignedContent(signedPdfData);
        CMSSignedData cms = new CMSSignedData(new CMSProcessableByteArray(signedContentAsBytes), signatureAsBytes);
        SignerInformation signerInfo = (SignerInformation) cms.getSignerInfos().getSigners().iterator().next();
        SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().build(certificate);
        assertThat(signerInfo.verify(verifier), is(true));
	}
	
	private static String convertToPem(X509Certificate cert) throws CertificateEncodingException {
		String pemCertPre = new String(Base64.getEncoder().encodeToString(cert.getEncoded()));
		return BEGIN_CERT + pemCertPre + END_CERT ;
	}
	
	private static void setUpKeys() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
		KeyPair keyPair = keyGen.generateKeyPair();
		X500Name x500Name = new X500Name("CN=test");
        SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(x500Name,
        		new BigInteger(10, new SecureRandom()), new Date(), new LocalDateTime().plusDays(1).toDate(), x500Name, pubKeyInfo
        );
        contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        certificate = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certificateBuilder.build(contentSigner));
	}
}
