package au.gov.qld.bdm.documentproduction.sign;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.jasig.cas.client.util.IOUtils;

public class CRLDistributionPointsExtractor {
	/**
	 * Extracts all CRL distribution point URLs from the
	 * "CRL Distribution Point" extension in a X.509 certificate. If CRL
	 * distribution point extension is unavailable, returns an empty list.
	 */
	public static List<String> getCrlDistributionPoints(X509Certificate cert) {

		ASN1InputStream oAsnInStream = null;
		ASN1InputStream oAsnInStream2 = null;
		try {
			byte[] crldpExt = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
			if (crldpExt == null) {
				List<String> emptyList = new ArrayList<String>();
				return emptyList;
			}
			oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));
			ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
			DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
			byte[] crldpExtOctets = dosCrlDP.getOctets();
			oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
			ASN1Primitive derObj2 = oAsnInStream2.readObject();
			CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
			List<String> crlUrls = new ArrayList<String>();
			for (DistributionPoint dp : distPoint.getDistributionPoints()) {
				DistributionPointName dpn = dp.getDistributionPoint();
				// Look for URIs in fullName
				if (dpn != null) {
					if (dpn.getType() == DistributionPointName.FULL_NAME) {
						GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
						// Look for an URI
						for (int j = 0; j < genNames.length; j++) {
							if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
								String url = DERIA5String.getInstance(genNames[j].getName()).getString();
								crlUrls.add(url);
							}
						}
					}
				}
			}
			return crlUrls;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			IOUtils.closeQuietly(oAsnInStream);
			IOUtils.closeQuietly(oAsnInStream2);
		}
	}
}