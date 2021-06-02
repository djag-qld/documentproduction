package au.gov.qld.bdm.documentproduction.sign.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.ServletTestExecutionListener;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {SpringBootDependencyInjectionTestExecutionListener.class, ServletTestExecutionListener.class})
public class SignatureRecordServiceTest {
	
	private static final byte[] DATA = "test".getBytes(StandardCharsets.UTF_8);
	private static final String ALGORITHM = "some algorithm";
	private static final String KEY = "some kms id";
	@Autowired SignatureRecordService service;

	private String agency;
	
	@Before
	public void setUp() {
		this.agency = RandomStringUtils.randomAlphanumeric(10);
	}
	
	@Test
	public void shouldStoreSignatureAndListForAgency() {
		service.storeSignature(DATA, ALGORITHM, KEY, agency);
		DataTablesInput input = new DataTablesInput();
		DataTablesOutput<SignatureView> output = service.list(input, agency);
		assertThat(output.getData().size(), is(1));
		assertThat(output.getData().get(0).getSignatureHex(), is(DigestUtils.sha256Hex(DATA)));
		assertThat(output.getData().get(0).getStatus(), is("VALID"));
		output = service.list(input, "bogus");
		assertThat(output.getData().size(), is(0));
	}
	
	@Test
	public void shouldVerifySignature() {
		service.storeSignature(DATA, ALGORITHM, KEY, agency);
		Optional<SignatureRecord> verify = service.verify(DigestUtils.sha256Hex(DATA), new AuditableCredential() {
			@Override
			public String getId() {
				return "some id";
			}

			@Override
			public String getAgency() {
				return agency;
			}
		});
		assertThat(verify.isPresent(), is(true));
		assertThat(verify.get().getStatus(), is("VALID"));
		
		verify = service.verify(DigestUtils.sha256Hex(DATA), new AuditableCredential() {
			@Override
			public String getId() {
				return "some id";
			}

			@Override
			public String getAgency() {
				return "bogus";
			}
		});
		assertThat(verify.isPresent(), is(false));
		
		verify = service.verify("bogus", new AuditableCredential() {
			@Override
			public String getId() {
				return "some id";
			}

			@Override
			public String getAgency() {
				return agency;
			}
		});
		assertThat(verify.isPresent(), is(false));
	}
}
