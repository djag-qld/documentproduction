package au.gov.qld.bdm.documentproduction.sign.validation;

import java.security.cert.X509Certificate;

public class CertSignatureInformation {
	private X509Certificate certificate;
	private String signatureHash;
	private boolean isSelfSigned;
	private String ocspUrl;
	private String crlUrl;
	private String issuerUrl;
	private X509Certificate issuerCertificate;
	private CertSignatureInformation certChain;
	private CertSignatureInformation tsaCerts;
	private CertSignatureInformation alternativeCertChain;

	public String getOcspUrl() {
		return ocspUrl;
	}

	public void setOcspUrl(String ocspUrl) {
		this.ocspUrl = ocspUrl;
	}

	public void setIssuerUrl(String issuerUrl) {
		this.issuerUrl = issuerUrl;
	}

	public String getCrlUrl() {
		return crlUrl;
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public boolean isSelfSigned() {
		return isSelfSigned;
	}

	public X509Certificate getIssuerCertificate() {
		return issuerCertificate;
	}

	public String getSignatureHash() {
		return signatureHash;
	}

	public CertSignatureInformation getCertChain() {
		return certChain;
	}

	public CertSignatureInformation getTsaCerts() {
		return tsaCerts;
	}

	public CertSignatureInformation getAlternativeCertChain() {
		return alternativeCertChain;
	}

	public void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public String getIssuerUrl() {
		return issuerUrl;
	}

	public void setSelfSigned(boolean selfSigned) {
		this.isSelfSigned = selfSigned;
	}

	public void setCrlUrl(String crlUrl) {
		this.crlUrl = crlUrl;
	}

	public void setIssuerCertificate(X509Certificate issuerCertificate) {
		this.issuerCertificate = issuerCertificate;
	}

	public void setCertChain(CertSignatureInformation certChain) {
		this.certChain = certChain;
	}

	public void setAlternativeCertChain(CertSignatureInformation alternativeCertChain) {
		this.alternativeCertChain = alternativeCertChain;
	}

	public void setTsaCerts(CertSignatureInformation tsaCerts) {
		this.tsaCerts = tsaCerts;
	}

	public void setSignatureHash(String signatureHash) {
		this.signatureHash = signatureHash;
	}
}
