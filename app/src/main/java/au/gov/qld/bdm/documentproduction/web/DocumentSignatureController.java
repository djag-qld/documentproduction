package au.gov.qld.bdm.documentproduction.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.bdm.documentproduction.document.DocumentSignatureService;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;

@Controller
public class DocumentSignatureController extends AdminListController {
	
	private final DocumentSignatureService service;
	private final SignatureKeyService signatureKeyService;

	@Autowired
	public DocumentSignatureController(DocumentSignatureService service, SignatureKeyService signatureKeyService, @Value("${security.require-ssl}") boolean secure) {
		super(secure);
		this.service = service;
		this.signatureKeyService = signatureKeyService;
	}
	
	@GetMapping("/user/documentsignature")
	public ModelAndView view(Principal principal, @CookieValue(defaultValue = "false", required = false) boolean hideInactive) {
		Map<String, Object> model = new HashMap<>();
		String agency = new WebAuditableCredential(principal).getAgency();
		model.put("items", service.list(agency, hideInactive));
		model.put("signatureKeyAliases", signatureKeyService.list(agency, true));
		model.put("hideInactive", hideInactive);
		return new ModelAndView("documentsignature", model);
	}
	
	@GetMapping("/user/documentsignature/toggleLatest")
	public RedirectView toggleLatest(@CookieValue(defaultValue = "false", required = false) boolean hideInactive, HttpServletResponse response) {
		return toggleAndRedirect(hideInactive, response);
	}
	
	@PostMapping("/user/documentsignature/add")
	public RedirectView add(Principal principal, @RequestParam String alias, @RequestParam String signatureKeyAlias, @RequestParam String reasonTemplate, 
			@RequestParam String signatoryTemplate, @RequestParam String locationTemplate, @RequestParam String contactInfoTemplate) {
		service.save(new WebAuditableCredential(principal), alias, signatureKeyAlias.split(" v:")[0], Integer.valueOf(signatureKeyAlias.split(" v:")[1]), reasonTemplate, signatoryTemplate, locationTemplate, contactInfoTemplate);
		return redirectToList();
	}

	@Override
	protected String getBase() {
		return "/user/documentsignature";
	}
	
}
