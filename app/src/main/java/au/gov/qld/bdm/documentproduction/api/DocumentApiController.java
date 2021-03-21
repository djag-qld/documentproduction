package au.gov.qld.bdm.documentproduction.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.gov.qld.bdm.documentproduction.api.entity.ApiKeyView;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.document.DocumentOutputFormat;
import au.gov.qld.bdm.documentproduction.document.DocumentService;
import freemarker.template.TemplateException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping(value = "/api", produces = { APPLICATION_JSON_VALUE })
@Api(consumes = "application/json", produces = "application/json", protocols = "https", value = "/api", tags = { "Document" } )
public class DocumentApiController {
	
	private final ApiKeyService apiKeyService;
	private final DocumentService documentService;

	@Autowired
	public DocumentApiController(ApiKeyService apiKeyService, DocumentService documentService) {
		this.apiKeyService = apiKeyService;
		this.documentService = documentService;
	}
	
	@ApiOperation(tags = "Document", value = "Generate a document", response = SignResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = SignResponse.class), @ApiResponse(code = 401, message = "Authorization failed") })
	@RequestMapping(value = "/document/object", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<DocumentResponse> documentWithResponse(@ApiIgnore @RequestHeader("Authorization") String apiKey, @RequestBody DocumentRequest request) throws GeneralSecurityException, IOException, TemplateException {
		Optional<AuditableCredential> credential = apiKeyToCredential(apiKey);
		if (!credential.isPresent()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);			
		}
		
		final DocumentResponse documentResponse = new DocumentResponse();
		try {
			final String documentId = documentService.record(credential.get(), request.getTemplateAlias(), request.getSignaturesAlias());
			documentResponse.setDocumentId(documentId);
			ByteArrayOutputStream pdfOs = new ByteArrayOutputStream();
			documentService.produce(credential.get(), documentId, request.getTemplateModel(), DocumentOutputFormat.PDF, pdfOs);
			if (request.getSignaturesAlias().isEmpty()) {
				documentResponse.setData(Base64.encodeBase64String(pdfOs.toByteArray()));
				return new ResponseEntity<>(documentResponse, HttpStatus.OK);
			}
			
			ByteArrayOutputStream signedOs = new ByteArrayOutputStream();
			ByteArrayInputStream pdf = new ByteArrayInputStream(pdfOs.toByteArray());
			documentService.sign(credential.get(), documentId, request.getTemplateModel(), pdf, signedOs);
			documentResponse.setData(Base64.encodeBase64String(signedOs.toByteArray()));
			return new ResponseEntity<>(documentResponse, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(documentResponse, HttpStatus.BAD_REQUEST);
		}
	}
	
	@ApiOperation(tags = "Document", value = "Generate a document", response = byte[].class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = byte[].class), @ApiResponse(code = 401, message = "Authorization failed") })
	@RequestMapping(value = "/document", produces = { MediaType.APPLICATION_PDF_VALUE }, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<byte[]> document(@ApiIgnore @RequestHeader("Authorization") String apiKey, @RequestBody DocumentRequest request, HttpServletResponse response) throws GeneralSecurityException, IOException, TemplateException {
		Optional<AuditableCredential> credential = apiKeyToCredential(apiKey);
		if (!credential.isPresent()) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);			
		}
		
		response.setHeader("Content-disposition", "attachment; filename=document.pdf");
		try {
			final String documentId = documentService.record(credential.get(), request.getTemplateAlias(), request.getSignaturesAlias());
			ByteArrayOutputStream pdfOs = new ByteArrayOutputStream();
			documentService.produce(credential.get(), documentId, request.getTemplateModel(), DocumentOutputFormat.PDF, pdfOs);
			if (request.getSignaturesAlias().isEmpty()) {
				return new ResponseEntity<>(pdfOs.toByteArray(), HttpStatus.OK);
			}
			
			ByteArrayOutputStream signedOs = new ByteArrayOutputStream();
			ByteArrayInputStream pdf = new ByteArrayInputStream(pdfOs.toByteArray());
			documentService.sign(credential.get(), documentId, request.getTemplateModel(), pdf, signedOs);
			return new ResponseEntity<>(signedOs.toByteArray(), HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(new byte[] {}, HttpStatus.BAD_REQUEST);
		}
	}

	private Optional<AuditableCredential> apiKeyToCredential(String apiKey) {
		Optional<ApiKeyView> apiKeyView = apiKeyService.validate(apiKey);
		if (!apiKeyView.isPresent()) {
			return Optional.empty();
		}
		
		AuditableCredential credential = new AuditableCredential() {
			@Override
			public String getId() {
				return apiKeyView.get().getApiKeyId();
			}

			@Override
			public String getAgency() {
				return apiKeyView.get().getAgency();
			}
		};
		return Optional.of(credential);
	}
}
