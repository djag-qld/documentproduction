package au.gov.qld.bdm.documentproduction.document;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class SignedQRContentTest {

	private static final String DOCUMENT_ID = "some document id";
	private static final Date CREATED = new LocalDate(2021, 10, 11).toDate();
	private static final Map<String, String> FIELDS = ImmutableMap.of("field", "field's \"value\"");
	private static final String KEY_ID = "some key id";
	private static final String SIGNATURE = "some signature";

	@Test
	public void shouldReturnJsonInConsistentOrderToAllowReliableSignatureVerificationOfRawData() {
		SignedQRContent content = new SignedQRContent(DOCUMENT_ID, CREATED, FIELDS);
		content.setKId(KEY_ID);
		// verify data that's used in client's signature verification
		assertThat(content.getAllContent(), is("{\"f\":{\"field\":\"field's \\\"value\\\"\"},\"dId\":\"some document id\",\"ver\":\"1.1.0\",\"cdate\":\"2021-10-11\",\"kid\":\"some key id\"}"));
		
		content.setSig(SIGNATURE);
		assertThat(content.getAllContent(), is("{\"f\":{\"field\":\"field's \\\"value\\\"\"},\"dId\":\"some document id\",\"ver\":\"1.1.0\",\"cdate\":\"2021-10-11\",\"sig\":\"some signature\",\"kid\":\"some key id\"}"));
	}
}
