package au.gov.qld.bdm.documentproduction.web;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.entity.AuditView;

@RestController
public class AuditController {
	
	private final AuditService service;

	@Autowired
	public AuditController(AuditService service) {
		this.service = service;
	}
	
	@ResponseBody
	@RequestMapping(value = "/user/audit/list/data", method = RequestMethod.POST)
    public DataTablesOutput<AuditView> listData(@Valid @RequestBody DataTablesInput input, Principal principal) {
		return service.list(input, new WebAuditableCredential(principal).getAgency());
    }
	
	@GetMapping("/user/audit")
	public ModelAndView view(Principal principal) {
		return new ModelAndView("audit");
	}
	
}
