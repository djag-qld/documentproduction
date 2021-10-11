package au.gov.qld.bdm.documentproduction.document;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class SignedQRContent {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@JsonIgnore
	private static final String VERSION = "1.1.0";

	@JsonProperty(index = 0, value = "f")
	private final SortedMap<String, String> fieldsFromTemplateModel;
	@JsonProperty(index = 1, value = "dId")
	private final String documentId;
	@JsonProperty(index = 2, value = "ver")
	private final String qrCodeVersion;
	@JsonProperty(index = 3, value = "cdate")
	private final String documentCreatedDate;
	@JsonProperty(index = 4, value = "sig")
	private String signatureOfAllFields;
	@JsonProperty(index = 5, value = "kid")
	private String keyAliasId;
	
	// For ObjectMapper
	@SuppressWarnings("unused")
	private SignedQRContent() {
		this.documentId = null;
		this.fieldsFromTemplateModel = null;
		this.qrCodeVersion = null;
		this.documentCreatedDate = null;
	}

	@JsonIgnore
	public SignedQRContent(String dId, Date createdDate, Map<String, String> f) {
		this.documentId = dId;
		this.documentCreatedDate = new LocalDate(createdDate).toString("yyyy-MM-dd");
		this.fieldsFromTemplateModel = new TreeMap<>(f);
		this.qrCodeVersion = VERSION;
	}

	public void setSig(String sig) {
		this.signatureOfAllFields = sig;
	}

	public void setKId(String kId) {
		this.keyAliasId = kId;
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
		return fieldsFromTemplateModel;
	}

	@JsonIgnore
	public String getSig() {
		return signatureOfAllFields;
	}
}
