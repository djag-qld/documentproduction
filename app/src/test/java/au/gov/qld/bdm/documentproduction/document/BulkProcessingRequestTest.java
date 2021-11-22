package au.gov.qld.bdm.documentproduction.document;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.gson.Gson;

public class BulkProcessingRequestTest {
	
	@Test
	public void shouldParseFullRequest() throws Exception {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bulkProcessingRequest.json");
		BulkProcessingRequest request = new Gson().fromJson(IOUtils.toString(inputStream, StandardCharsets.UTF_8), BulkProcessingRequest.class);
		assertThat(request.getAgency(), is("some agency"));
		assertThat(request.getSignatureAlias(), is(Arrays.asList("alias a", "alias b")));
		assertThat(request.getTemplateAlias(), is("some template alias"));
		assertThat(request.getTemplateModel(), hasEntry("a field", "a value"));
		assertThat(request.getTemplateModel(), hasEntry("b field", "b value"));
	}
	
	@Test
	public void shouldParseRequestWithoutSignature() throws Exception {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bulkProcessingRequestWithoutSignature.json");
		BulkProcessingRequest request = new Gson().fromJson(IOUtils.toString(inputStream, StandardCharsets.UTF_8), BulkProcessingRequest.class);
		assertThat(request.getAgency(), is("some agency"));
		assertThat(request.getSignatureAlias(), nullValue());
		assertThat(request.getTemplateAlias(), is("some template alias"));
		assertThat(request.getTemplateModel(), hasEntry("a field", "a value"));
		assertThat(request.getTemplateModel(), hasEntry("b field", "b value"));
	}
}
