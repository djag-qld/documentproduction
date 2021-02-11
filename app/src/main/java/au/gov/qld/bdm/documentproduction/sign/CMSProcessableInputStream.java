package au.gov.qld.bdm.documentproduction.sign;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cms.CMSTypedData;

public class CMSProcessableInputStream implements CMSTypedData {
    private final InputStream inputStream;
    private final ASN1ObjectIdentifier contentType;

    CMSProcessableInputStream(InputStream is) {
        this(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), is);
    }

    private CMSProcessableInputStream(ASN1ObjectIdentifier type, InputStream is) {
        contentType = type;
        inputStream = is;
    }

    @Override
    public Object getContent() {
        return inputStream;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        IOUtils.copy(inputStream, out);
        inputStream.close();
    }

    @Override
    public ASN1ObjectIdentifier getContentType() {
        return contentType;
    }
}
