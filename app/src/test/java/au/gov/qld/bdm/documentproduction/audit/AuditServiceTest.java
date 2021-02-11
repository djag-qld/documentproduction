package au.gov.qld.bdm.documentproduction.audit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.gov.qld.bdm.documentproduction.audit.entity.AuditActionRepository;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AuditServiceTest {
	@Autowired AuditService service;
	@Autowired AuditActionRepository repository;
	
	@Test
	public void shouldInsertAuditActions() {
		repository.deleteAll();
		AuditableCredential auditableCredential = new AuditableCredential() {
			@Override
			public String getId() {
				return "test id";
			}

			@Override
			public String getAgency() {
				return "test agency";
			}
		};
		String event = RandomStringUtils.randomAlphabetic(10);
		service.audit(AuditBuilder.of(event).from(auditableCredential).target("id", "target", "type").build());
		DataTablesInput input = new DataTablesInput();
		input.setLength(100);
		assertThat(service.list(input, "test agency").getData().iterator().next().getEvent(), is(event));
	}
}
