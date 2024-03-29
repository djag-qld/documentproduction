package au.gov.qld.bdm.documentproduction.web;

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

import com.lowagie.text.DocumentException;

import au.gov.qld.bdm.documentproduction.document.DocumentService;
import au.gov.qld.bdm.documentproduction.template.TemplateService;
import au.gov.qld.bdm.documentproduction.template.entity.TemplateView;
import freemarker.template.TemplateException;

@Controller
public class TemplateController extends AdminListController {
	
	private final TemplateService service;
	private final DocumentService documentService;

	@Autowired
	public TemplateController(TemplateService service, DocumentService documentService, @Value("${security.require-ssl}") boolean secure) {
		super(secure);
		this.service = service;
		this.documentService = documentService;
	}
	
	@GetMapping("/user/template")
	public ModelAndView view(Principal principal, @CookieValue(defaultValue = "false", required = false) boolean hideInactive) {
		Map<String, Object> model = new HashMap<>();
		model.put("items", service.list(new WebAuditableCredential(principal).getAgency(), hideInactive));
		model.put("hideInactive", hideInactive);
		return new ModelAndView("template", model);
	}
	
	@GetMapping("/user/template/toggleLatest")
	public RedirectView toggleLatest(@CookieValue(defaultValue = "false", required = false) boolean hideInactive, HttpServletResponse response) {
		return toggleAndRedirect(hideInactive, response);
	}
	
	@PostMapping("/user/template/add")
	public RedirectView add(Principal principal, @RequestParam String alias, @RequestParam String content) {
		service.save(new WebAuditableCredential(principal), alias, content);
		return redirectToList();
	}
	
	@GetMapping("/user/template/view/{alias}/{version}")
	public void view(Principal principal, @PathVariable String alias, @PathVariable int version, HttpServletResponse response) throws IOException {
        Optional<TemplateView> view = service.findByAliasAndVersionAndAgency(alias, version, new WebAuditableCredential(principal).getAgency());
        if (!view.isPresent()) {
        	throw new IllegalArgumentException("Could not find by alias, version and agency");
        }

        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Content-Disposition", "attachment; filename=" + view.get().getAlias() + "-" + view.get().getVersion() + ".txt");
        IOUtils.write(view.get().getContent(), response.getOutputStream(), StandardCharsets.UTF_8);
	}
	
	@GetMapping("/user/template/preview/{template}/{version}")
	public void preview(Principal principal, @PathVariable String template, @PathVariable int version, HttpServletResponse response) throws IOException, TemplateException, DocumentException {		
		response.setHeader("Cache-Control", "must-revalidate");
		try {
			response.setContentType("application/pdf");
			response.setHeader("Content-disposition", "attachment; filename=preview.pdf");
			documentService.preview(new WebAuditableCredential(principal), template, version, response.getOutputStream());
		} catch (Exception e) {
			response.setHeader("Content-disposition", "attachment; filename=error.txt");
			response.setContentType("text/plain");
			IOUtils.write(e.getMessage(), response.getOutputStream(), StandardCharsets.UTF_8);
		}
	}

	@Override
	protected String getBase() {
		return "/user/template";
	}
	
}
