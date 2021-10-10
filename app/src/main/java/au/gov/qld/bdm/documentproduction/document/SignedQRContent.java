package au.gov.qld.bdm.documentproduction.document;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;

import com.google.gson.Gson;

public class SignedQRContent {

	private static final String VERSION = "1.1.0";

	private final SortedMap<String, String> f;
	private final String dId;
	private final String ver;
	private final String cdate;
	private String sig;
	private String kid;

	public SignedQRContent(String dId, Date createdDate, Map<String, String> f) {
		this.dId = dId;
		this.cdate = new LocalDate(createdDate).toString("yyyy-MM-dd");
		this.f = new TreeMap<>(f);
		this.ver = VERSION;
	}

	public SortedMap<String, String> getF() {
		return f;
	}

	public String getSig() {
		return sig;
	}

	public String getKId() {
		return kid;
	}

	public String getDId() {
		return dId;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public void setKId(String kId) {
		this.kid = kId;
	}

	public String getAllContent() {
		return new Gson().toJson(this);
	}

	public String getVer() {
		return ver;
	}
	
	public String getCDate() {
		return cdate;
	}
}
