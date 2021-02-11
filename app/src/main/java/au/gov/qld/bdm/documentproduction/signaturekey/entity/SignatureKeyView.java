package au.gov.qld.bdm.documentproduction.signaturekey.entity;

import java.util.Date;

public interface SignatureKeyView {
	String getAlias();
	int getVersion();
	String getCreatedBy();
	Date getCreated();
	String getTimestampEndpoint();
}
