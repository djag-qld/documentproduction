package au.gov.qld.bdm.documentproduction.web;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
public class AuditControllerTest {

	@Autowired MockMvc mockMvc;
	
	@Test
	@WithMockUser(username = "user1", password = "pwd", roles = "USER")
	public void shouldReturnAuditData() throws Exception {
		mockMvc.perform(post("/user/apikey/add?apiKey=test.1234").with(csrf().asHeader())).andDo(print()).andExpect(status().is3xxRedirection());
		
		DataTablesInput input = new DataTablesInput();
		List<Column> columns = asList(new Column("target", "target", true, true, new Search("", false)));
		input.setColumns(columns );
		input.setDraw(1);
		input.setLength(10);
		input.setStart(0);
		mockMvc.perform(post("/user/audit/list/data").with(csrf().asHeader()).content(asJsonString(input)).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
			.andExpect(content().string(containsString("test")));
	}
	
	public static String asJsonString(final Object obj) {
	    try {
	        return new ObjectMapper().writeValueAsString(obj);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
