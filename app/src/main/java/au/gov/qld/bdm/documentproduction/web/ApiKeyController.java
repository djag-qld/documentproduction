package au.gov.qld.bdm.documentproduction.web;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.bdm.documentproduction.api.ApiKeyService;

@Controller
public class ApiKeyController {
	
	private final ApiKeyService service;

	@Autowired
	public ApiKeyController(ApiKeyService service) {
		this.service = service;
	}
	
	@GetMapping("/user/apikey")
	public ModelAndView view(Principal principal) {
		Map<String, Object> model = new HashMap<>();
		model.put("items", service.list(new WebAuditableCredential(principal).getAgency()));
		
		String apiKeyId = generateSafeToken(10);
		String apiKey = apiKeyId + "." + generateSafeToken(53);
		
		model.put("apiKey", apiKey);
		return new ModelAndView("apikey", model);
	}
	
	@PostMapping("/user/apikey/add")
	public RedirectView add(Principal principal, @RequestParam String apiKey) {
		service.save(new WebAuditableCredential(principal), apiKey);
		return redirectToList();
	}
	
	@PostMapping("/user/apikey/toggle")
	public RedirectView toggle(Principal principal, @RequestParam String apiKeyId) {
		service.toggleEnabled(new WebAuditableCredential(principal), apiKeyId);
		return redirectToList();
	}

	private RedirectView redirectToList() {
		RedirectView redirectView = new RedirectView("/user/apikey");
		redirectView.setExposeModelAttributes(false);
		redirectView.setExposePathVariables(false);
		return redirectView;
	}
	
	private String generateSafeToken(int length) {
		return RandomStringUtils.random(length, 0, 0, true, true, null, new SecureRandom());
	}
	
}
