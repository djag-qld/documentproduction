package au.gov.qld.bdm.documentproduction.sign.validation;

import java.security.cert.X509Certificate;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OcspKey {
	private final X509Certificate checkCertificate;
	private final X509Certificate issuerCertificate;
	private final Set<X509Certificate> additionalCerts;
	private final String ocspUrl;
	
	public OcspKey(X509Certificate certificateToCheck, X509Certificate issuerCertificate, Set<X509Certificate> additionalCerts, String ocspUrl) {
		this.checkCertificate = certificateToCheck;
		this.issuerCertificate = issuerCertificate;
		this.additionalCerts = additionalCerts;
		this.ocspUrl = ocspUrl;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public X509Certificate getCheckCertificate() {
		return checkCertificate;
	}

	public X509Certificate getIssuerCertificate() {
		return issuerCertificate;
	}

	public Set<X509Certificate> getAdditionalCerts() {
		return additionalCerts;
	}

	public String getOcspUrl() {
		return ocspUrl;
	}
}
