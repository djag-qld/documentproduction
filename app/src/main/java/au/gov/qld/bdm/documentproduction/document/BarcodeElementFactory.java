package au.gov.qld.bdm.documentproduction.document;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

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

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.document.entity.Document;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

public class BarcodeElementFactory extends ITextReplacedElementFactory {
	public static final String IMG_TYPE_ATTRIBUTE = "type";
	public static final String BARCODE_IMG_TYPE = "barcode";
	public static final String QRCODE_IMG_TYPE = "qrcode";
	public static final String SIGNED_QRCODE_IMG_TYPE = "signedqrcode";
	public static final String IMG_TAG = "img";
	public static final String SRC_ATTRIBUTE = "src";
	private final Document document;
	private final Map<String, String> templateModel;
	private final SignedQRCodeService signedQRCodeService;
	private final AuditableCredential credential;
	
	public BarcodeElementFactory(ITextOutputDevice outputDevice, AuditableCredential credential, Document document, Map<String, String> templateModel, SignedQRCodeService signedQRCodeService) {
		super(outputDevice);
		this.credential = credential;
		this.document = document;
		this.templateModel = templateModel;
		this.signedQRCodeService = signedQRCodeService;
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
			String qrContent = signedQRCodeService.create(credential, document, element.getAttribute(SRC_ATTRIBUTE), templateModel);
			return createQRCode(cssWidth, cssHeight, qrContent );			
		}

		return super.createReplacedElement(c, box, uac, cssWidth, cssHeight);
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
