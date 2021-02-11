package au.gov.qld.bdm.documentproduction.sign;

import java.security.PublicKey;

import org.bouncycastle.operator.ContentSigner;

import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

public interface ContentSignerFactory {

	ContentSigner create(SignatureKey key, CertificateResponse certificate);

	PublicKey getPublicKey(SignatureKey key);

	ContentSigner create(SignatureKey signatureKey);

}
