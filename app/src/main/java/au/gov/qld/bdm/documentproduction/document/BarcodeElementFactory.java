package au.gov.qld.bdm.documentproduction.document;

import java.awt.Color;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class BarcodeElementFactory extends ITextReplacedElementFactory {
	private static final Logger LOG = LoggerFactory.getLogger(BarcodeElementFactory.class);
	
	public static final String IMG_TYPE_ATTRIBUTE = "type";
	public static final String BARCODE_IMG_TYPE = "barcode";
	public static final String BARCODE_ELEMENT_TAG = "img";
	public static final String BARCODE_NUMBER_ATTRIBUTE = "src";
	
	public BarcodeElementFactory(ITextOutputDevice outputDevice) {
		super(outputDevice);
	}

	@Override
	public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
		Element element = box.getElement();
		if (!BARCODE_ELEMENT_TAG.equals(element.getNodeName()) || !BARCODE_IMG_TYPE.equals(element.getAttribute(IMG_TYPE_ATTRIBUTE))) {
			return super.createReplacedElement(c, box, uac, cssWidth, cssHeight);
		}

		Barcode128 barcode = new Barcode128();
		barcode.setCode(element.getAttribute(BARCODE_NUMBER_ATTRIBUTE));
		LOG.debug("Generating barcode for: {}", element.getTextContent());
		try {
			FSImage fsImage = new ITextFSImage(Image.getInstance(barcode.createAwtImage(Color.BLACK, Color.WHITE), Color.WHITE));
			if (cssWidth > 0 && cssHeight > 0) {
	            fsImage.scale(cssWidth, cssHeight);
	          }
	          return new ITextImageElement(fsImage);
		} catch (BadElementException | IOException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
}
