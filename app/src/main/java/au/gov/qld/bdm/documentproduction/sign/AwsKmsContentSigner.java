package au.gov.qld.bdm.documentproduction.sign;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.MessageType;
import com.amazonaws.services.kms.model.SignRequest;
import com.amazonaws.services.kms.model.SignResult;
import com.amazonaws.services.kms.model.SigningAlgorithmSpec;

import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;

public class AwsKmsContentSigner implements ContentSigner {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final String region;
	private final String key;
	private final AlgorithmIdentifier signatureAlgorithm;
	private final SigningAlgorithmSpec signingAlgorithmSpec;
	private final SignatureRecordService signatureRecordServicey;
	private final String agency;

    public AwsKmsContentSigner(String region, String key, SigningAlgorithmSpec signingAlgorithmSpec, SignatureRecordService signatureRecordService, String agency) {
        this.region = region;
		this.key = key;
		this.signingAlgorithmSpec = signingAlgorithmSpec;
		this.signatureRecordServicey = signatureRecordService;
		this.agency = agency;
        this.signatureAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find(signingAlgorithmNameBySpec.get(signingAlgorithmSpec));
    }

    @Override
    public byte[] getSignature() {
    	AWSKMS kmsClient = AWSKMSClientBuilder.standard().withRegion(region).build();
    	ByteBuffer message = ByteBuffer.wrap(outputStream.toByteArray());
		SignRequest signRequest = new SignRequest()
    			.withSigningAlgorithm(signingAlgorithmSpec)
    			.withKeyId(key)
    			.withMessageType(MessageType.RAW)
    			.withMessage(message);
		SignResult signResult = kmsClient.sign(signRequest);
		signatureRecordServicey.storeSignature(signResult.getSignature(), signingAlgorithmSpec.name(), key, agency);
		return signResult.getSignature().array();
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return signatureAlgorithm;
    }

    private final static Map<SigningAlgorithmSpec, String> signingAlgorithmNameBySpec;
    static {
        signingAlgorithmNameBySpec = new HashMap<>();
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.ECDSA_SHA_256, "SHA256withECDSA");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.ECDSA_SHA_384, "SHA384withECDSA");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.ECDSA_SHA_512, "SHA512withECDSA");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256, "SHA256withRSA");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_384, "SHA384withRSA");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_512, "SHA512withRSA");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.RSASSA_PSS_SHA_256, "SHA256withRSAandMGF1");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.RSASSA_PSS_SHA_384, "SHA384withRSAandMGF1");
        signingAlgorithmNameBySpec.put(SigningAlgorithmSpec.RSASSA_PSS_SHA_512, "SHA512withRSAandMGF1");
    }
}