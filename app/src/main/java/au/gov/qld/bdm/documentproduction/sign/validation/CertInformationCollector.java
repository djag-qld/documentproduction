package au.gov.qld.bdm.documentproduction.sign.validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.examples.signature.validation.CertificateProccessingException;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This class helps to extract data/information from a signature. The information is held in
 * CertSignatureInformation. Some information is needed for validation processing of the
 * participating certificates.
 *
 * @author Alexis Suter
 *
 */
public class CertInformationCollector
{
    private static final Logger LOG = LoggerFactory.getLogger(CertInformationCollector.class);
    
    private static final LoadingCache<String, X509Certificate> ISSUER_CERTS = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(CertificateVerifier.CACHE_PERIOD, TimeUnit.SECONDS)
		.build(new CacheLoader<String, X509Certificate>() {
			@Override
			public X509Certificate load(String issuerUrl) throws IOException, GeneralSecurityException {
				URL certUrl = new URL(issuerUrl);
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = certUrl.openStream();
				LOG.info("Fetching issuer certificate: {}", issuerUrl);
				X509Certificate altIssuerCert = (X509Certificate) certFactory.generateCertificate(in);
				in.close();
				return altIssuerCert;
			}
		});

    private static final int MAX_CERTIFICATE_CHAIN_DEPTH = 5;

    private final Set<X509Certificate> certificateSet = new HashSet<X509Certificate>();
    private final Set<String> urlSet = new HashSet<String>();

    private final JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();

    private CertSignatureInformation rootCertInfo;

    /**
     * Gets the certificate information of a signature.
     * 
     * @param signature the signature of the document.
     * @param fileName of the document.
     * @return the CertSignatureInformation containing all certificate information
     * @throws CertificateProccessingException when there is an error processing the certificates
     * @throws IOException on a data processing error
     */
    public CertSignatureInformation getLastCertInfo(PDSignature signature, InputStream is)
            throws CertificateProccessingException, IOException
    {
        try
        {
            byte[] signatureContent = signature.getContents(is);
            return getCertInfo(signatureContent);
        }
        finally
        {
            is.close();
        }
    }

    /**
     * Processes one signature and its including certificates.
     *
     * @param signatureContent the byte[]-Content of the signature
     * @return the CertSignatureInformation for this signature
     * @throws IOException
     * @throws CertificateProccessingException
     */
    private CertSignatureInformation getCertInfo(byte[] signatureContent)
            throws CertificateProccessingException, IOException
    {
        rootCertInfo = new CertSignatureInformation();

        rootCertInfo.setSignatureHash(CertInformationHelper.getSha1Hash(signatureContent));

        try
        {
            CMSSignedData signedData = new CMSSignedData(signatureContent);
            SignerInformation signerInformation = processSignerStore(signedData, rootCertInfo);
            addTimestampCerts(signerInformation);
        }
        catch (CMSException e)
        {
            LOG.error("Error occurred getting Certificate Information from Signature", e);
            throw new CertificateProccessingException(e);
        }
        return rootCertInfo;
    }

    /**
     * Processes an embedded signed timestamp, that has been placed into a signature. The
     * certificates and its chain(s) will be processed the same way as the signature itself.
     *
     * @param signerInformation of the signature, to get unsigned attributes from it.
     * @throws IOException
     * @throws CertificateProccessingException
     */
    private void addTimestampCerts(SignerInformation signerInformation)
            throws IOException, CertificateProccessingException
    {
        AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
        if (unsignedAttributes == null)
        {
            return;
        }
        Attribute tsAttribute = unsignedAttributes.get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
        if (tsAttribute == null)
        {
            return;
        }
        ASN1Encodable obj0 = tsAttribute.getAttrValues().getObjectAt(0);
        if (!(obj0 instanceof ASN1Object))
        {
            return;
        }
        ASN1Object tsSeq = (ASN1Object) obj0;

        try
        {
            CMSSignedData signedData = new CMSSignedData(tsSeq.getEncoded("DER"));
            rootCertInfo.setTsaCerts(new CertSignatureInformation());
            processSignerStore(signedData, rootCertInfo.getTsaCerts());
        }
        catch (CMSException e)
        {
            throw new IOException("Error parsing timestamp token", e);
        }
    }

    /**
     * Processes a signer store and goes through the signers certificate-chain. Adds the found data
     * to the certInfo. Handles only the first signer, although multiple would be possible, but is
     * not yet practicable.
     *
     * @param signedData data from which to get the SignerInformation
     * @param certInfo where to add certificate information
     * @return Signer Information of the processed certificatesStore for further usage.
     * @throws IOException on data-processing error
     * @throws CertificateProccessingException on a specific error with a certificate
     */
    private SignerInformation processSignerStore(
            CMSSignedData signedData, CertSignatureInformation certInfo)
            throws IOException, CertificateProccessingException
    {
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        SignerInformation signerInformation = signers.iterator().next();

        Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        @SuppressWarnings("unchecked")
        Collection<X509CertificateHolder> matches = certificatesStore
                .getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());

        X509Certificate certificate = getCertFromHolder(matches.iterator().next());
        certificateSet.add(certificate);

        Collection<X509CertificateHolder> allCerts = certificatesStore.getMatches(null);
        addAllCerts(allCerts);
        traverseChain(certificate, certInfo, MAX_CERTIFICATE_CHAIN_DEPTH);
        return signerInformation;
    }
    
    static void getAuthorityInfoExtensionValue(byte[] extensionValue,
            CertSignatureInformation certInfo) throws IOException
    {
        ASN1Sequence asn1Seq = (ASN1Sequence) JcaX509ExtensionUtils.parseExtensionValue(extensionValue);
        Enumeration<?> objects = asn1Seq.getObjects();
        while (objects.hasMoreElements())
        {
            // AccessDescription
            ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
            ASN1Encodable oid = obj.getObjectAt(0);
            // accessLocation
            ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);

            if (X509ObjectIdentifiers.id_ad_ocsp.equals(oid) && location.getTagNo() == GeneralName.uniformResourceIdentifier)
            {
                ASN1OctetString url = (ASN1OctetString) location.getObject();
                certInfo.setOcspUrl(new String(url.getOctets()));
            }
            else if (X509ObjectIdentifiers.id_ad_caIssuers.equals(oid))
            {
                ASN1OctetString uri = (ASN1OctetString) location.getObject();
                certInfo.setIssuerUrl(new String(uri.getOctets()));
            }
        }
    }
    
    static String getCrlUrlFromExtensionValue(byte[] extensionValue) throws IOException
    {
        ASN1Sequence asn1Seq = (ASN1Sequence) JcaX509ExtensionUtils.parseExtensionValue(extensionValue);
        Enumeration<?> objects = asn1Seq.getObjects();

        while (objects.hasMoreElements())
        {
            Object obj = objects.nextElement();
            if (obj instanceof ASN1Sequence)
            {
                String url = extractCrlUrlFromSequence((ASN1Sequence) obj);
                if (url != null)
                {
                    return url;
                }
            }
        }
        return null;
    }
    
    static String extractCrlUrlFromSequence(ASN1Sequence sequence)
    {
        ASN1TaggedObject taggedObject = (ASN1TaggedObject) sequence.getObjectAt(0);
        taggedObject = (ASN1TaggedObject) taggedObject.getObject();
        if (taggedObject.getObject() instanceof ASN1TaggedObject)
        {
            taggedObject = (ASN1TaggedObject) taggedObject.getObject();
        }
        else if (taggedObject.getObject() instanceof ASN1Sequence)
        {
            // multiple URLs (we take the first)
            ASN1Sequence seq = (ASN1Sequence) taggedObject.getObject();
            if (seq.getObjectAt(0) instanceof ASN1TaggedObject)
            {
                taggedObject = (ASN1TaggedObject) seq.getObjectAt(0);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
        if (taggedObject.getObject() instanceof ASN1OctetString)
        {
            ASN1OctetString uri = (ASN1OctetString) taggedObject.getObject();
            String url = new String(uri.getOctets());

            // return first http(s)-Url for crl
            if (url.startsWith("http"))
            {
                return url;
            }
        }
        // else happens with http://blogs.adobe.com/security/SampleSignedPDFDocument.pdf
        return null;
    }

    /**
     * Traverse through the Cert-Chain of the given Certificate and add it to the CertInfo
     * recursively.
     *
     * @param certificate Actual Certificate to be processed
     * @param certInfo where to add the Certificate (and chain) information
     * @param maxDepth Max depth from this point to go through CertChain (could be infinite)
     * @throws IOException on data-processing error
     * @throws CertificateProccessingException on a specific error with a certificate
     */
    private void traverseChain(X509Certificate certificate, CertSignatureInformation certInfo,
            int maxDepth) throws IOException, CertificateProccessingException
    {
        certInfo.setCertificate(certificate);

        // Certificate Authority Information Access
        // As described in https://tools.ietf.org/html/rfc3280.html#section-4.2.2.1
        byte[] authorityExtensionValue = certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (authorityExtensionValue != null)
        {
            getAuthorityInfoExtensionValue(authorityExtensionValue, certInfo);
        }
        
        if (certInfo.getIssuerUrl() != null)
        {
            getAlternativeIssuerCertificate(certInfo, maxDepth);
        }

        // As described in https://tools.ietf.org/html/rfc3280.html#section-4.2.1.14
        byte[] crlExtensionValue = certificate.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crlExtensionValue != null)
        {
            certInfo.setCrlUrl(getCrlUrlFromExtensionValue(crlExtensionValue));
        }

        try
        {
            certInfo.setSelfSigned(CertificateVerifier.isSelfSigned(certificate));
        }
        catch (GeneralSecurityException ex)
        {
            throw new CertificateProccessingException(ex);
        }
        if (maxDepth <= 0 || certInfo.isSelfSigned())
        {
            return;
        }

        for (X509Certificate issuer : certificateSet)
        {
            try
            {
                certificate.verify(issuer.getPublicKey(), SecurityProvider.getProvider().getName());
                LOG.debug("Found the right Issuer Cert! for Cert: " + certificate.getSubjectX500Principal()
                    + "\n" + issuer.getSubjectX500Principal());
                certInfo.setIssuerCertificate(issuer);
                certInfo.setCertChain(new CertSignatureInformation());
                traverseChain(issuer, certInfo.getCertChain(), maxDepth - 1);
                break;
            }
            catch (GeneralSecurityException ex)
            {
                // not the issuer
            }                
        }
        if (certInfo.getIssuerCertificate() == null)
        {
            LOG.debug(
                    "No Issuer Certificate found for Cert: '" +
                            certificate.getSubjectX500Principal() + "', i.e. Cert '" +
                            certificate.getIssuerX500Principal() + "' is missing in the chain");
        }
    }

    /**
     * Get alternative certificate chain, from the Authority Information (a url). If the chain is
     * not included in the signature, this is the main chain. Otherwise there might be a second
     * chain. Exceptions which happen on this chain will be logged and ignored, because the cert
     * might not be available at the time or other reasons.
     *
     * @param certInfo base Certificate Information, on which to put the alternative Certificate
     * @param maxDepth Maximum depth to dig through the chain from here on.
     * @throws CertificateProccessingException on a specific error with a certificate
     * @throws IOException 
     */
    private void getAlternativeIssuerCertificate(CertSignatureInformation certInfo, int maxDepth)
            throws CertificateProccessingException, IOException
    {
        if (urlSet.contains(certInfo.getIssuerUrl()))
        {
            return;
        }
        urlSet.add(certInfo.getIssuerUrl());
        LOG.debug("Get alternative issuer certificate from: " + certInfo.getIssuerUrl());
        X509Certificate altIssuerCert;
		try {
			altIssuerCert = ISSUER_CERTS.get(certInfo.getIssuerUrl());
			certificateSet.add(altIssuerCert);

	        certInfo.setAlternativeCertChain(new CertSignatureInformation());
	        traverseChain(altIssuerCert, certInfo.getAlternativeCertChain(), maxDepth - 1);
		} catch (ExecutionException | IOException e) {
			throw new IOException(e.getCause());
		}
    }

    /**
     * Gets the X509Certificate out of the X509CertificateHolder.
     *
     * @param certificateHolder to get the certificate from
     * @return a X509Certificate or <code>null</code> when there was an Error with the Certificate
     * @throws CertificateProccessingException on failed conversion from X509CertificateHolder to
     * X509Certificate
     */
    private X509Certificate getCertFromHolder(X509CertificateHolder certificateHolder)
            throws CertificateProccessingException
    {
        try
        {
            return certConverter.getCertificate(certificateHolder);
        }
        catch (CertificateException e)
        {
            LOG.error("Certificate Exception getting Certificate from certHolder.", e);
            throw new CertificateProccessingException(e);
        }
    }

    /**
     * Adds multiple Certificates out of a Collection of X509CertificateHolder into certificateSet.
     *
     * @param certHolders Collection of X509CertificateHolder
     */
    private void addAllCerts(Collection<X509CertificateHolder> certHolders)
    {
        for (X509CertificateHolder certificateHolder : certHolders)
        {
            try
            {
                X509Certificate certificate = getCertFromHolder(certificateHolder);
                certificateSet.add(certificate);
            }
            catch (CertificateProccessingException e)
            {
                LOG.warn("Certificate Exception getting Certificate from certHolder.", e);
            }
        }
    }

    /**
     * Gets a list of X509Certificate out of an array of X509CertificateHolder. The certificates
     * will be added to certificateSet.
     *
     * @param certHolders Array of X509CertificateHolder
     * @throws CertificateProccessingException when one of the Certificates could not be parsed.
     */
    public void addAllCertsFromHolders(X509CertificateHolder[] certHolders)
            throws CertificateProccessingException
    {
        addAllCerts(Arrays.asList(certHolders));
    }

    /**
     * Traverse a certificate.
     *
     * @param certificate
     * @return
     * @throws CertificateProccessingException 
     */
    CertSignatureInformation getCertInfo(X509Certificate certificate) throws CertificateProccessingException
    {
        try
        {
            CertSignatureInformation certSignatureInformation = new CertSignatureInformation();
            traverseChain(certificate, certSignatureInformation, MAX_CERTIFICATE_CHAIN_DEPTH);
            return certSignatureInformation;
        }
        catch (IOException ex)
        {
            throw new CertificateProccessingException(ex);
        }
    }

    /**
     * Get the set of all processed certificates until now.
     * 
     * @return a set of serial numbers to certificates.
     */
    public Set<X509Certificate> getCertificateSet()
    {
        return certificateSet;
    }

}
