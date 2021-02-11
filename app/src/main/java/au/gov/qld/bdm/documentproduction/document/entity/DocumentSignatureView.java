package au.gov.qld.bdm.documentproduction.document.entity;

import java.util.Date;

import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKeyView;

public interface DocumentSignatureView {
	String getAlias();
	String getCreatedBy();
	Date getCreated();
	String getReasonTemplate();
	String getSignatoryTemplate();
	String getLocationTemplate();
	String getContactInfoTemplate();
	SignatureKeyView getSignatureKey();
}
