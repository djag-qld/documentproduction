package au.gov.qld.bdm.documentproduction.document;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.render.BlockBox;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.document.entity.Document;
import net.glxn.qrgen.core.image.ImageType;

@RunWith(MockitoJUnitRunner.class)
public class BarcodeElementFactoryTest {
	private static final String KEY_ALIAS = "some key alias";
	private static final int SCALED_WIDTH = 123;
	private static final int SCALED_HEIGHT = 456;
	private static final int DATA_LENGTH = 375;
	
	BarcodeElementFactory factory;
	Map<String, String> templateModel;
	
	@Mock ITextOutputDevice outputDevice;
	@Mock LayoutContext c;
	@Mock BlockBox box;
	@Mock UserAgentCallback uac;
	@Mock Element barcodeElement;
	@Mock Document document;
	@Mock SignedQRCodeService signedQRCodeService;
	@Mock AuditableCredential credential;
	
	@Before
	public void setUp() {
		templateModel = new HashMap<>();
		when(barcodeElement.getAttribute("src")).thenReturn("some barcode number");
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.BARCODE_IMG_TYPE);
		when(barcodeElement.getNodeName()).thenReturn(BarcodeElementFactory.IMG_TAG);
		when(box.getElement()).thenReturn(barcodeElement);
		
		factory = new BarcodeElementFactory(outputDevice, credential, document, templateModel, signedQRCodeService);
	}
	
	@Test
	public void shouldCreateQRCodeWhenTriggeredFromBarcodeElementWithQRType() {
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.QRCODE_IMG_TYPE);
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(SCALED_WIDTH));
		assertThat(image.getImage().getHeight(), is(SCALED_HEIGHT));
	}
	
	@Test
	public void shouldCreateQRCodeWhenTriggeredFromBarcodeElementWithSignedQRType() {
		when(barcodeElement.getAttribute("src")).thenReturn(KEY_ALIAS);
		when(signedQRCodeService.create(credential, document, KEY_ALIAS, templateModel)).thenReturn("some qr code content");
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.SIGNED_QRCODE_IMG_TYPE);
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(SCALED_WIDTH));
		assertThat(image.getImage().getHeight(), is(SCALED_HEIGHT));
	}
	
	@Test
	public void shouldCreateSignedQRCodeWithSpecifiedPixels() {
		when(barcodeElement.getAttribute("src")).thenReturn(KEY_ALIAS);
		when(barcodeElement.getAttribute("imagetype")).thenReturn(String.valueOf(ImageType.BMP)); // no compression makes it more reliable to test
		when(barcodeElement.getAttribute("qrpixels")).thenReturn(String.valueOf(BarcodeElementFactory.QRCODE_MAX_PIXELS));
		when(signedQRCodeService.create(credential, document, KEY_ALIAS, templateModel)).thenReturn("some qr code content");
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.SIGNED_QRCODE_IMG_TYPE);
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(SCALED_WIDTH));
		assertThat(image.getImage().getHeight(), is(SCALED_HEIGHT));
		assertThat(((ITextFSImage)image.getImage()).getImage().getRawData().length, is(DATA_LENGTH * BarcodeElementFactory.QRCODE_MAX_PIXELS));
	}
	
	@Test
	public void shouldCreateQRCodeWithSpecifiedPixels() {
		when(barcodeElement.getAttribute("src")).thenReturn("qr value");
		when(barcodeElement.getAttribute("imagetype")).thenReturn(String.valueOf(ImageType.BMP)); // no compression makes it more reliable to test
		when(barcodeElement.getAttribute("qrpixels")).thenReturn(String.valueOf(BarcodeElementFactory.QRCODE_MAX_PIXELS));
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.QRCODE_IMG_TYPE);
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(SCALED_WIDTH));
		assertThat(image.getImage().getHeight(), is(SCALED_HEIGHT));
		assertThat(((ITextFSImage)image.getImage()).getImage().getRawData().length, is(DATA_LENGTH * BarcodeElementFactory.QRCODE_MAX_PIXELS));
	}
	
	@Test
	public void shouldCreateQRCodeWithSpecifiedImageType() {
		when(barcodeElement.getAttribute("src")).thenReturn("qr value");
		when(barcodeElement.getAttribute("imagetype")).thenReturn(String.valueOf(ImageType.BMP));
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.QRCODE_IMG_TYPE);
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(SCALED_WIDTH));
		assertThat(image.getImage().getHeight(), is(SCALED_HEIGHT));
		assertThat(((ITextFSImage)image.getImage()).getImage().getRawData().length, is(2000));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptoinWhenQRCodePixelsTooHigh() {
		when(barcodeElement.getAttribute("qrpixels")).thenReturn(String.valueOf(BarcodeElementFactory.QRCODE_MAX_PIXELS + 1));
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.SIGNED_QRCODE_IMG_TYPE);
		factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptoinWhenQRCodePixelsInvalid() {
		when(barcodeElement.getAttribute("qrpixels")).thenReturn(String.valueOf(0));
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.SIGNED_QRCODE_IMG_TYPE);
		factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
	}

	@Test
	public void shouldCreateBarcodeWhenTriggeredFromBarcodeElement() {
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(SCALED_WIDTH));
		assertThat(image.getImage().getHeight(), is(SCALED_HEIGHT));
	}
	
	@Test
	public void shouldCreateBarcodeWhenTriggeredFromBarcodeElementWithDefaultSize() {
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, -1, -1);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(244));
		assertThat(image.getImage().getHeight(), is(24));
	}
	
	@Test
	public void shouldNotCreateBarcodeWhenNotTriggeredFromBarcodeElement() {
		when(barcodeElement.getNodeName()).thenReturn("other tag");
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, SCALED_WIDTH, SCALED_HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(false));
	}
}
