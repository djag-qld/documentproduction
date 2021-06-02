package au.gov.qld.bdm.documentproduction.sign.repository;

import java.util.Date;

public interface SignatureView {
	String getId();
	Date getCreatedAt();
	Date getLastModifiedAt();
	String getKeyId();
	String getSignatureHex();
	String getSignatureHexAlgorithm();
	String getSignatureAlgorithm();
	String getStatus();
}
