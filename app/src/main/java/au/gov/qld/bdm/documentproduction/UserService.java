package au.gov.qld.bdm.documentproduction;

import static java.util.Arrays.asList;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.web.RoleHelper;
import au.gov.qld.bdm.documentproduction.web.WebAuditableCredential;

@Service
public class UserService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	private final String roleRequired;
	private final AuditService auditService;

	@Autowired
	public UserService(@Value("${cas.role}") String roleRequired, AuditService auditService) {
		this.roleRequired = roleRequired;
		this.auditService = auditService;
	}
	
	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		final String login = token.getPrincipal().toString();
		Set<String> roles = RoleHelper.extractRoles(token.getAssertion().getPrincipal());
		if (roles.contains(roleRequired)) {
			LOG.info("User: {} logged in", login);
			WebAuditableCredential credential = new WebAuditableCredential(token.getAssertion().getPrincipal());
			auditService.audit(AuditBuilder.of("login").from(credential).target(credential.getId(), "administration", "web").build());
			return new User(login, "password", true, true, true, true, asList(new SimpleGrantedAuthority(roleRequired)));
		}

		LOG.warn("User: {} logged in without role: {}", login, roleRequired);
		throw new UsernameNotFoundException("User does not have role: " + roleRequired);
}

}