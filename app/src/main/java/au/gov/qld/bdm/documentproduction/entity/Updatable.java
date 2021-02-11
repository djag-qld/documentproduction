package au.gov.qld.bdm.documentproduction.entity;

public interface Updatable {
	boolean isLatest();
	void setLatest(boolean latest);
	int getVersion();
	void setVersion(int version);
}
