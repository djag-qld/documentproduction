package au.gov.qld.bdm.documentproduction.web;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jasig.cas.client.authentication.AttributePrincipal;

public final class RoleHelper {
    public static final String ROLES_ATTRIBUTE = "roles";
    public static final String AGENCY_ATTRIBUTE = "agency";

	private RoleHelper() {
    }

    public static Set<String> extractRoles(AttributePrincipal attributePrincipal) {
        if (attributePrincipal == null) {
            return Collections.emptySet();
        }
        
        String raw = (String)attributePrincipal.getAttributes().get(ROLES_ATTRIBUTE);
        Set<String> roles = new HashSet<>();
        for (String role : defaultString(raw).split(",")) {
            roles.add(role.trim());
        }
        return roles;
    }
    
    public static String extractAgency(AttributePrincipal principal) {
		return (String) principal.getAttributes().get(AGENCY_ATTRIBUTE);
    }
}
