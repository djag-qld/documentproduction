package au.gov.qld.bdm.documentproduction.web;

import java.security.Principal;
import java.util.HashMap;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.bdm.documentproduction.document.DocumentService;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentView;

@Controller
public class DocumentController {
	
	private final DocumentService service;

	@Autowired
	public DocumentController(DocumentService service) {
		this.service = service;
	}
	
	@GetMapping("/user/document")
	public ModelAndView home(Principal principal) {
		HashMap<String, Object> model = new HashMap<>();
		return new ModelAndView("document", model);
	}
	
	@ResponseBody
	@RequestMapping(value = "/user/document/list/data", method = RequestMethod.POST)
    public DataTablesOutput<DocumentView> listData(@Valid @RequestBody DataTablesInput input, Principal principal) {
		return service.list(input, new WebAuditableCredential(principal).getAgency());
    }
	
	@GetMapping("/user")
	public RedirectView redirectToHome() {
		RedirectView redirectView = new RedirectView("/user/document");
		redirectView.setExposeModelAttributes(false);
		redirectView.setExposePathVariables(false);
		return redirectView;
	}
	
	@GetMapping("/")
	public RedirectView redirectToHomeFromBase() {
		return redirectToHome();
	}
}
