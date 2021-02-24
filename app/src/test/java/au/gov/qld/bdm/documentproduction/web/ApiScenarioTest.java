package au.gov.qld.bdm.documentproduction.web;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import au.gov.qld.bdm.documentproduction.api.DocumentRequest;
import au.gov.qld.bdm.documentproduction.api.DocumentResponse;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiScenarioTest {

	@Autowired MockMvc mockMvc;
	
	@Test
	@WithMockUser(username = "user1", password = "pwd", roles = "USER")
	public void shouldReturnDocument() throws Exception {
		String signatureKeyAlias = RandomStringUtils.randomAlphanumeric(10);
		String documentSignatureAlias = RandomStringUtils.randomAlphanumeric(10);
		String templateAlias = RandomStringUtils.randomAlphanumeric(10);
		
		String apiKey = mockMvc.perform(get("/user/apikey")).andReturn().getModelAndView().getModel().get("apiKey").toString();
		mockMvc.perform(post("/user/apikey/add?apiKey=" + apiKey).with(csrf().asHeader())).andDo(print()).andExpect(status().is3xxRedirection());
		mockMvc.perform(post("/user/signaturekey/add").param("alias", signatureKeyAlias).param("kmsId", "stub").param("certificate", "some cert")
				.with(csrf().asHeader())).andDo(print()).andExpect(status().is3xxRedirection());
		
		mockMvc.perform(post("/user/documentsignature/add").param("alias", documentSignatureAlias).param("signatoryTemplate", "signature template")
				.param("reasonTemplate", "reason template").param("signatureKeyAlias", signatureKeyAlias + " v:1")
				.param("locationTemplate", "location template").param("contactInfoTemplate", "contact info template")
				.with(csrf().asHeader())).andDo(print()).andExpect(status().is3xxRedirection());
		mockMvc.perform(post("/user/template/add").param("alias", templateAlias).param("content", "<html>test content</html>")
				.with(csrf().asHeader())).andDo(print()).andExpect(status().is3xxRedirection());
		
		DocumentRequest documentRequest = new DocumentRequest();
		documentRequest.setTemplateAlias(templateAlias);
		documentRequest.setTemplateModel(ImmutableMap.of("name", "someone"));
		MockHttpServletResponse response = mockMvc.perform(post("/api/document/object").header("Authorization", apiKey).content(asJsonString(documentRequest)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
			.andExpect(content().string(containsString("documentId"))).andExpect(content().string(containsString("data"))).andReturn().getResponse();
		
		DocumentResponse documentResponse = new Gson().fromJson(response.getContentAsString(), DocumentResponse.class);
		assertThat(documentResponse.getDocumentId().length(), greaterThan(1));
		PDDocument pdDocument = PDDocument.load(Base64.decodeBase64(documentResponse.getData()));
		PDFTextStripper pdfStripper = new PDFTextStripper();
		String content = pdfStripper.getText(pdDocument);
		pdDocument.close();
		
		assertThat(content, containsString("test content"));
		
		response = mockMvc.perform(post("/api/document").header("Authorization", apiKey).content(asJsonString(documentRequest)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_PDF)).andDo(print()).andExpect(status().isOk()).andReturn().getResponse();
		pdDocument = PDDocument.load(response.getContentAsByteArray());
		content = pdfStripper.getText(pdDocument);
		assertThat(content, containsString("test content"));
		pdDocument.close();
	}
	
	public static String asJsonString(final Object obj) {
	    try {
	        return new ObjectMapper().writeValueAsString(obj);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
