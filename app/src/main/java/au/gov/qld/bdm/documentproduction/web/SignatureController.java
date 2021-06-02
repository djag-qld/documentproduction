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

import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureView;

@Controller
public class SignatureController {
	
	private final SignatureRecordService service;

	@Autowired
	public SignatureController(SignatureRecordService service) {
		this.service = service;
	}
	
	@GetMapping("/user/signature")
	public ModelAndView home(Principal principal) {
		HashMap<String, Object> model = new HashMap<>();
		return new ModelAndView("signature", model);
	}
	
	@ResponseBody
	@RequestMapping(value = "/user/signature/list/data", method = RequestMethod.POST)
    public DataTablesOutput<SignatureView> listData(@Valid @RequestBody DataTablesInput input, Principal principal) {
		return service.list(input, new WebAuditableCredential(principal).getAgency());
    }
	
}
