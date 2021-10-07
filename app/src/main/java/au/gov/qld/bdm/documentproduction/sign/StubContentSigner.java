package au.gov.qld.bdm.documentproduction.sign;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.RandomUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;

public class StubContentSigner implements ContentSigner {

	private final byte[] randomSignature = RandomUtils.nextBytes(20);
	
	@Override
	public AlgorithmIdentifier getAlgorithmIdentifier() {
		return new AlgorithmIdentifier(new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1")); //sha256
	}

	@Override
	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}

	@Override
	public byte[] getSignature() {
		return randomSignature;
	}

}
