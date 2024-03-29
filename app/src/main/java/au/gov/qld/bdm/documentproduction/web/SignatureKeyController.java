package au.gov.qld.bdm.documentproduction.web;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.bdm.documentproduction.sign.SigningService;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

@Controller
public class SignatureKeyController extends AdminListController {
	private final SignatureKeyService service;
	private final SigningService signingService;
	private final String subjectdn;

	@Autowired
	public SignatureKeyController(SignatureKeyService service, SigningService signingService, @Value("${sign.subjectdn}") String subjectdn, @Value("${security.require-ssl}") boolean secure) {
		super(secure);
		this.service = service;
		this.signingService = signingService;
		this.subjectdn = subjectdn;
	}
	
	@GetMapping("/user/signaturekey/toggleLatest")
	public RedirectView toggleLatest(@CookieValue(defaultValue = "false", required = false) boolean hideInactive, HttpServletResponse response) {
		return toggleAndRedirect(hideInactive, response);
	}
	
	@GetMapping("/user/signaturekey")
	public ModelAndView view(Principal principal, @CookieValue(defaultValue = "false", required = false) boolean hideInactive) {
		Map<String, Object> model = new HashMap<>();
		model.put("items", service.list(new WebAuditableCredential(principal).getAgency(), hideInactive));
		model.put("defaultSubjectdn", subjectdn);
		model.put("hideInactive", hideInactive);
		return new ModelAndView("signatureKey", model);
	}
	
	@GetMapping("/user/signaturekey/{alias}/certificate/{version}")
	public void certificate(Principal principal, @PathVariable String alias, @PathVariable int version, HttpServletResponse response) throws IOException {		
        Optional<SignatureKey> signatureKey = service.findKeyForAlias(new WebAuditableCredential(principal).getAgency(), alias, version);
        if (!signatureKey.isPresent()) {
        	throw new IllegalArgumentException("Could not find by alias, version and agency");
        }
        
        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Content-Disposition", "attachment; filename=" + signatureKey.get().getAlias() + ".cer");		
		IOUtils.write(signatureKey.get().getCertificate(), response.getOutputStream(), StandardCharsets.UTF_8);
	}
	
	@PostMapping("/user/signaturekey/add")
	public RedirectView add(Principal principal, @RequestParam String alias, @RequestParam String kmsId, @RequestParam(required = false) String certificate, @RequestParam(required = false) String timestampEndpoint) {
		service.save(new WebAuditableCredential(principal), alias, kmsId, defaultString(certificate), timestampEndpoint);
		return redirectToList();
	}
	
	@PostMapping("/user/signaturekey/csr")
	public void csr(Principal principal, @RequestParam String alias, @RequestParam String subjectdn, HttpServletResponse response) throws IOException {
		WebAuditableCredential credential = new WebAuditableCredential(principal);
        Optional<SignatureKey> signatureKey = service.findKeyForAlias(credential.getAgency(), alias.split(" v:")[0], Integer.valueOf(alias.split(" v:")[1]));
		if (!signatureKey.isPresent()) {
			throw new IllegalArgumentException("Could not find by alias, version and agency");
        }
		
		response.setContentType("text/plain");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Content-Disposition", "attachment; filename=" + signatureKey.get().getAlias() + ".csr");
		IOUtils.write(signingService.generateCsr(signatureKey.get(), credential, subjectdn), response.getOutputStream(), StandardCharsets.UTF_8);
	}

	@Override
	protected String getBase() {
		return "/user/signaturekey";
	}
	
}
