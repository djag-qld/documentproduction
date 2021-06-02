package au.gov.qld.bdm.documentproduction.sign;

import java.security.PublicKey;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.GetPublicKeyRequest;
import com.amazonaws.services.kms.model.GetPublicKeyResult;
import com.amazonaws.services.kms.model.SigningAlgorithmSpec;

import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

@Service
public class AwsContentSignerFactory implements ContentSignerFactory {

    private final String region;
	private final SignatureRecordService signatureRecordService;
    
	public AwsContentSignerFactory(@Value("${aws.kms.region}") String region, SignatureRecordService signatureRecordService) {
		this.region = region;
		this.signatureRecordService = signatureRecordService;
	}
	
	@Override
	public ContentSigner create(SignatureKey key) {
		if ("stub".equals(this.region)) {
			return new StubContentSigner();
		}
		
		return new AwsKmsContentSigner(region, key.getKmsId(), SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256, signatureRecordService);
	}
	
	@Override
	public ContentSigner create(SignatureKey key, CertificateResponse certificate) {
		if ("stub".equals(this.region)) {
			return new StubContentSigner();
		}
		
		return new AwsKmsContentSigner(region, key.getKmsId(), certificate.getAlgorithm(), signatureRecordService);
	}

	@Override
	public PublicKey getPublicKey(SignatureKey key) {
		if ("stub".equals(this.region)) {
			return null;
		}
		
		AWSKMS kmsClient = AWSKMSClientBuilder.standard().withRegion(region).build();
		GetPublicKeyResult response = kmsClient.getPublicKey(new GetPublicKeyRequest().withKeyId(key.getKmsId()));
		SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(response.getPublicKey().array());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		try {
			return converter.getPublicKey(spki);
		} catch (PEMException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

}
