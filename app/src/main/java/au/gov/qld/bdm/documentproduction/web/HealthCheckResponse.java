package au.gov.qld.bdm.documentproduction.web;

public class HealthCheckResponse {
	public static final HealthCheckResponse OK = new HealthCheckResponse();
	static {
		OK.setOkay(true);
	}
	
	private boolean okay;
	private String version;

	public boolean isOkay() {
		return okay;
	}

	public void setOkay(boolean okay) {
		this.okay = okay;
	}

	public HealthCheckResponse withVersion(String version) {
		this.setVersion(version);
		return this;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public HealthCheckResponse okay() {
		this.setOkay(true);
		return this;
	}
}
