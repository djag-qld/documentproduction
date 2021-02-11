package au.gov.qld.bdm.documentproduction.sign;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaCRLStore;

import com.amazonaws.services.kms.model.SigningAlgorithmSpec;

public class CertificateResponse {
	private final List<X509Certificate> certificates;
	private final SigningAlgorithmSpec signingAlgorithmSpec;
	
	public CertificateResponse(List<X509Certificate> certificates, SigningAlgorithmSpec signingAlgorithmSpec) {
		// spec should be: RSASSA_PKCS1_V1_5_SHA_256
		this.certificates = certificates;
		this.signingAlgorithmSpec = signingAlgorithmSpec;
	}

	@SuppressWarnings("unchecked")
	public CertificateResponse(String certificate) throws CertificateException {
		this.signingAlgorithmSpec = SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256;
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Collection<? extends Certificate> certificates = certFactory.generateCertificates(new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8)));
		this.certificates = (List<X509Certificate>) certificates;
	}

	public List<X509Certificate> getCertificates() {
		return certificates;
	}

	public SigningAlgorithmSpec getAlgorithm() {
		return signingAlgorithmSpec;
	}

	public X509Certificate getSignerCertificate() {
		return certificates.get(certificates.size() - 1);
	}

	public JcaCRLStore getCrls() throws GeneralSecurityException, IOException {
		return new JcaCRLStore(fetchCRLs(getSignerCertificate()));
	}
	
	private Collection<X509CRL> fetchCRLs(X509Certificate signingCert)
			throws CertificateException, MalformedURLException, CRLException, IOException {
		List<String> crlList = CRLDistributionPointsExtractor.getCrlDistributionPoints(signingCert);
		List<X509CRL> crls = new ArrayList<X509CRL>();
		for (String crlUrl : crlList) {
			if (!crlUrl.startsWith("http")) {
				continue;
			}
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			URL url = new URL(crlUrl);
			X509CRL crl = (X509CRL) cf.generateCRL(url.openStream());
			crls.add(crl);
		}
		return crls;
	}

}
