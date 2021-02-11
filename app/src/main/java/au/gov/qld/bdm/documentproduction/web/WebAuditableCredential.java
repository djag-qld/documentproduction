package au.gov.qld.bdm.documentproduction.web;

import java.security.Principal;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.security.cas.authentication.CasAuthenticationToken;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;

public class WebAuditableCredential implements AuditableCredential {

	private final String login;
	private final String agency;

	public WebAuditableCredential(AttributePrincipal principal) {
		this.login = principal.toString();
		this.agency = RoleHelper.extractAgency(principal);
	}
	
	public WebAuditableCredential(Principal principal) {
		if (!(principal instanceof CasAuthenticationToken)) {
			this.agency = "unknown";
			this.login = principal.getName();
			return;
		}
		CasAuthenticationToken token = (CasAuthenticationToken) principal;
		this.login = token.getAssertion().getPrincipal().getName();
		AttributePrincipal attributePrincipal = ((CasAuthenticationToken) principal).getAssertion().getPrincipal();
		this.agency = RoleHelper.extractAgency(attributePrincipal);
	}

	@Override
	public String getId() {
		return login;
	}
	
	@Override
	public String getAgency() {
		return agency;
	}

}
