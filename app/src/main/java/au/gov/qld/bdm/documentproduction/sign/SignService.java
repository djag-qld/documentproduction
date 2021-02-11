package au.gov.qld.bdm.documentproduction.sign;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.GetPublicKeyRequest;
import com.amazonaws.services.kms.model.GetPublicKeyResult;
import com.amazonaws.services.kms.model.SigningAlgorithmSpec;

@Service
public class SignService {
	private static final Logger LOG = LoggerFactory.getLogger(SignService.class);
	
	private final String region;
	private final String subjectDn;

	public SignService(@Value("${aws.kms.region}") String region, @Value("${sign.subjectdn}") String subjectDn) {
		this.region = region;
		this.subjectDn = subjectDn;
	}

	public CertificateResponse generateSelfSignedCertificate(String key, String subjectDN) throws IOException, GeneralSecurityException {
		long now = System.currentTimeMillis();
		Date startDate = new Date(now);

		LOG.info("Generating cert under subject: {}", subjectDN);
		X500Name dnName = new X500Name(subjectDN);
		BigInteger certSerialNumber = new BigInteger(Long.toString(now));

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.YEAR, 1);
		Date endDate = calendar.getTime();

		AWSKMS kmsClient = AWSKMSClientBuilder.standard().withRegion(region).build();
		GetPublicKeyResult response = kmsClient.getPublicKey(new GetPublicKeyRequest().withKeyId(key));
		SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(response.getPublicKey().array());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		PublicKey publicKey = converter.getPublicKey(spki);
		List<String> signingAlgorithms = response.getSigningAlgorithms();
		SigningAlgorithmSpec signingAlgorithmSpec = null;
		if (signingAlgorithms != null && !signingAlgorithms.isEmpty()) {
			signingAlgorithmSpec = SigningAlgorithmSpec.fromValue(signingAlgorithms.get(0));
		}

		ContentSigner contentSigner = new AwsKmsContentSigner(region, key, signingAlgorithmSpec);
		
		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate,
				endDate, dnName, publicKey);
		// breaks the signature of the self signed
		//generateCsr(publicKey, contentSigner);
		
		BasicConstraints basicConstraints = new BasicConstraints(true);
		certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);
		
		return new CertificateResponse(
				asList(new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(contentSigner))),
				signingAlgorithmSpec);
	}

	@SuppressWarnings("unused")
	private void generateCsr(PublicKey publicKey, ContentSigner contentSigner) throws IOException {
		PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(subjectDn), publicKey);
		PKCS10CertificationRequest csr = p10Builder.build(contentSigner);

		PemObject pemObject = new PemObject("CERTIFICATE REQUEST", csr.getEncoded());
		StringWriter csrString = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(csrString);
		pemWriter.writeObject(pemObject);
		pemWriter.close();
		csrString.close();
		LOG.info("CSR:\n{}", csrString.toString());
	}
}
