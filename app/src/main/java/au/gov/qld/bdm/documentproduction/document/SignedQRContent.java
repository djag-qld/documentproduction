package au.gov.qld.bdm.documentproduction.document;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SignedQRContent {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@JsonIgnore
	private static final String VERSION = "1.1.0";

	@JsonProperty(index = 0, value = "f")
	private final SortedMap<String, String> f;
	@JsonProperty(index = 1, value = "dId")
	private final String dId;
	@JsonProperty(index = 2, value = "ver")
	private final String ver;
	@JsonProperty(index = 3, value = "cdate")
	private final String cdate;
	@JsonProperty(index = 4, value = "sig")
	private String sig;
	@JsonProperty(index = 5, value = "kid")
	private String kid;

	@JsonIgnore
	public SignedQRContent(String dId, Date createdDate, Map<String, String> f) {
		this.dId = dId;
		this.cdate = new LocalDate(createdDate).toString("yyyy-MM-dd");
		this.f = new TreeMap<>(f);
		this.ver = VERSION;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public void setKId(String kId) {
		this.kid = kId;
	}

	@JsonIgnore
	public String getAllContent() {
		try {
			return MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@JsonIgnore
	public Map<String, String> getF() {
		return f;
	}

	@JsonIgnore
	public String getSig() {
		return sig;
	}
}
