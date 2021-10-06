package au.gov.qld.bdm.documentproduction.sign;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;

public class StubContentSigner implements ContentSigner {

	@Override
	public AlgorithmIdentifier getAlgorithmIdentifier() {
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}

	@Override
	public byte[] getSignature() {
		return new byte[] {};
	}

}
