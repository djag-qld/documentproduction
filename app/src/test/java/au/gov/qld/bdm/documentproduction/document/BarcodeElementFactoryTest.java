package au.gov.qld.bdm.documentproduction.document;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.render.BlockBox;

@RunWith(MockitoJUnitRunner.class)
public class BarcodeElementFactoryTest {
	private static final int WIDTH = 123;
	private static final int HEIGHT = 456;
	
	BarcodeElementFactory factory;
	
	@Mock ITextOutputDevice outputDevice;
	@Mock LayoutContext c;
	@Mock BlockBox box;
	@Mock UserAgentCallback uac;
	@Mock Element barcodeElement;
	
	@Before
	public void setUp() {
		when(barcodeElement.getAttribute("src")).thenReturn("some barcode number");
		when(barcodeElement.getAttribute("type")).thenReturn(BarcodeElementFactory.BARCODE_IMG_TYPE);
		when(barcodeElement.getNodeName()).thenReturn(BarcodeElementFactory.BARCODE_ELEMENT_TAG);
		when(box.getElement()).thenReturn(barcodeElement);
		
		factory = new BarcodeElementFactory(outputDevice);
	}

	@Test
	public void shouldCreateBarcodeWhenTriggeredFromBarcodeElement() {
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, WIDTH, HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(true));
		ITextImageElement image = (ITextImageElement) replaced;
		assertThat(image.getImage().getWidth(), is(WIDTH));
		assertThat(image.getImage().getHeight(), is(HEIGHT));
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
		ReplacedElement replaced = factory.createReplacedElement(c, box, uac, WIDTH, HEIGHT);
		assertThat(replaced instanceof ITextImageElement, is(false));
	}
}
