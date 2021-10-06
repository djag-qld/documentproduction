package au.gov.qld.bdm.documentproduction.document;

import java.util.SortedMap;

import com.google.gson.Gson;

public class SignedQRContent {

	private SortedMap<String, String> f;
	private String sig;
	private String kid;
	private String dId;
	private String ver;
	private String cdate;

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

	public void setF(SortedMap<String, String> f) {
		this.f = f;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public void setKId(String kId) {
		this.kid = kId;
	}

	public String getSignatureContent() {
		return new Gson().toJson(f);
	}

	public String getAllContent() {
		return new Gson().toJson(this);
	}

	public void setDId(String documentId) {
		this.dId = documentId;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}
	
	public String getVer() {
		return ver;
	}

	public void setCDate(String cDate) {
		this.cdate = cDate;
	}
	
	public String getCDate() {
		return cdate;
	}
}
