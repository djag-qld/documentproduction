package au.gov.qld.bdm.documentproduction.document;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.Deflater;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.operator.ContentSigner;
import org.joda.time.LocalDate;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextReplacedElementFactory;
import org.xhtmlrenderer.render.BlockBox;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.Barcode128;

import au.gov.qld.bdm.documentproduction.document.entity.Document;
import au.gov.qld.bdm.documentproduction.sign.ContentSignerFactory;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import nl.minvws.encoding.Base45;

public class BarcodeElementFactory extends ITextReplacedElementFactory {
	private static final String SIGNED_QRCODE_VERSION = "1.0.0";
	public static final String IMG_TYPE_ATTRIBUTE = "type";
	public static final String BARCODE_IMG_TYPE = "barcode";
	public static final String QRCODE_IMG_TYPE = "qrcode";
	public static final String SIGNED_QRCODE_IMG_TYPE = "signedqrcode";
	public static final String IMG_TAG = "img";
	public static final String SRC_ATTRIBUTE = "src";
	private final Document document;
	private final Map<String, String> templateModel;
	private final SignatureKeyService signatureKeyService;
	private final ContentSignerFactory contentSignerFactory;
	private final SignatureRecordService signatureRecordService;
	
	public BarcodeElementFactory(ITextOutputDevice outputDevice, Document document, Map<String, String> templateModel, SignatureKeyService signatureKeyService,
			ContentSignerFactory contentSignerFactory, SignatureRecordService signatureRecordService) {
		super(outputDevice);
		this.document = document;
		this.templateModel = templateModel;
		this.signatureKeyService = signatureKeyService;
		this.contentSignerFactory = contentSignerFactory;
		this.signatureRecordService = signatureRecordService;
	}

	@Override
	public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
		Element element = box.getElement();
		if (!IMG_TAG.equals(element.getNodeName())) {
			return super.createReplacedElement(c, box, uac, cssWidth, cssHeight);	
		}
		
		if (BARCODE_IMG_TYPE.equals(element.getAttribute(IMG_TYPE_ATTRIBUTE))) {
			return createBarcode128(cssWidth, cssHeight, element.getAttribute(SRC_ATTRIBUTE));
		}
		
		if (QRCODE_IMG_TYPE.equals(element.getAttribute(IMG_TYPE_ATTRIBUTE))) {
			return createQRCode(cssWidth, cssHeight, element.getAttribute(SRC_ATTRIBUTE));
		}
		
		if (SIGNED_QRCODE_IMG_TYPE.equals(element.getAttribute(IMG_TYPE_ATTRIBUTE))) {
			Optional<SignatureKey> keyForAlias = signatureKeyService.findKeyForAlias(document.getAgency(), element.getAttribute(SRC_ATTRIBUTE));
			if (!keyForAlias.isPresent()) {
				throw new IllegalArgumentException("No signature key available for that src");
			}
			
			SignedQRContent qrContent = new SignedQRContent();
			qrContent.setF(new TreeMap<>(templateModel));
			qrContent.setKId(keyForAlias.get().getAlias() + ":" + keyForAlias.get().getVersion());
			qrContent.setDId(document.getId());
			qrContent.setVer(SIGNED_QRCODE_VERSION);
			qrContent.setCDate(new LocalDate().toString("yyyy-MM-dd"));
			try {
				ContentSigner contentSigner = contentSignerFactory.create(keyForAlias.get());
				IOUtils.write(qrContent.getSignatureContent(), contentSigner.getOutputStream(), StandardCharsets.UTF_8);
				signatureRecordService.storeSignature(contentSigner.getSignature(), contentSigner.getAlgorithmIdentifier().getAlgorithm().getId(), 
						keyForAlias.get().getKmsId(), document.getAgency());
				
				String encodedSignature = Base45.getEncoder().encodeToString(contentSigner.getSignature());
				qrContent.setSig(encodedSignature);
				return createQRCode(cssWidth, cssHeight, compress(qrContent.getAllContent().getBytes(StandardCharsets.UTF_8)));
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}

		return super.createReplacedElement(c, box, uac, cssWidth, cssHeight);
	}
	
	private String compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(data);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		return Base45.getEncoder().encodeToString(outputStream.toByteArray());
	}

	private ReplacedElement createQRCode(int width, int height, String content) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		QRCode.from(content).to(ImageType.PNG).writeTo(os);
		try {
			return convertToScaledITextImage(Image.getInstance(os.toByteArray()), width, height);
		} catch (BadElementException | IOException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	private ReplacedElement createBarcode128(int width, int height, String content) {
		Barcode128 barcode = new Barcode128();
		barcode.setCode(content);
		try {
			return convertToScaledITextImage(Image.getInstance(barcode.createAwtImage(Color.BLACK, Color.WHITE), Color.WHITE), width, height);
		} catch (BadElementException | IOException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
	
	private ReplacedElement convertToScaledITextImage(Image image, int width, int height) {
		FSImage fsImage = new ITextFSImage(image);
		if (width > 0 && height > 0) {
            fsImage.scale(width, height);
        }
        return new ITextImageElement(fsImage);
	}
}
