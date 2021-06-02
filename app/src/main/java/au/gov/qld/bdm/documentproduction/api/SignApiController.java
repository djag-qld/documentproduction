package au.gov.qld.bdm.documentproduction.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import au.gov.qld.bdm.documentproduction.sign.SigningService;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecord;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;
import au.gov.qld.bdm.documentproduction.web.SigningRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping(value = "/api", produces = { APPLICATION_JSON_VALUE })
@Api(consumes = "application/json", produces = "application/json", protocols = "https", value = "/api", tags = { "Signature" } )
public class SignApiController {
	private static final Logger LOG = LoggerFactory.getLogger(SignApiController.class);
	
	private final SigningService signingService;
	private final SignatureKeyService keyService;
	private final ApiKeyService apiKeyService;
	private final SignatureRecordService signatureRecordService;

	@Autowired
	public SignApiController(SigningService signingService, SignatureKeyService keyService, ApiKeyService apiKeyService, SignatureRecordService signatureRecordService) {
		this.signingService = signingService;
		this.keyService = keyService;
		this.apiKeyService = apiKeyService;
		this.signatureRecordService = signatureRecordService;
	}
	
	@ApiOperation(tags = "Signature", value = "Apply signature to a document", response = byte[].class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = byte[].class), @ApiResponse(code = 401, message = "Authorization failed") })
	@RequestMapping(value = "/sign", produces = { MediaType.APPLICATION_PDF_VALUE }, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<byte[]> sign(@ApiIgnore @RequestHeader("Authorization") String apiKey, @RequestBody SigningRequest signRequest, HttpServletResponse response) throws GeneralSecurityException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int responseCode = verifyAndSign(apiKey, signRequest, os);		
		if (responseCode == HttpStatus.OK.value()) {
			response.setHeader("Content-disposition", "attachment; filename=signed.pdf");
		}
		
		return new ResponseEntity<>(os.toByteArray(), HttpStatus.valueOf(responseCode));
	}
	
	@ApiOperation(tags = "Signature", value = "Apply signature to a document with response object", response = SignResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = SignResponse.class), @ApiResponse(code = 401, message = "Authorization failed") })
	@RequestMapping(value = "/sign/object", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<SignResponse> signWithResponse(@ApiIgnore @RequestHeader("Authorization") String apiKey, @RequestBody SigningRequest signRequest) throws GeneralSecurityException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int responseCode = verifyAndSign(apiKey, signRequest, os);
		if (responseCode != HttpStatus.OK.value()) {
			return new ResponseEntity<>(HttpStatus.valueOf(responseCode));
		}
		
		SignResponse signResponse = new SignResponse();
		signResponse.setData(Base64.encodeBase64String(os.toByteArray()));
		return new ResponseEntity<>(signResponse, HttpStatus.valueOf(responseCode));
	}
	
	@ApiOperation(tags = "Signature", value = "Verify signature", response = VerifyResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = VerifyResponse.class), @ApiResponse(code = 401, message = "Authorization failed") })
	@RequestMapping(value = "/sign/verify", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<VerifyResponse> verify(@ApiIgnore @RequestHeader("Authorization") String apiKey, @RequestBody VerifyRequest verifyRequest) {
		Optional<AuditableCredential> credential = apiKeyToCredential(apiKey);
		if (!credential.isPresent()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();			
		}
		
		Optional<SignatureRecord> optional = signatureRecordService.verify(verifyRequest.getSignatureHex(), credential.get());
		if (optional.isPresent()) {
			VerifyResponse responseBody = new VerifyResponse();
			responseBody.setCreatedAt(optional.get().getCreatedAt());
			responseBody.setLastModifiedAt(optional.get().getLastModifiedAt());
			responseBody.setStatus(optional.get().getStatus());
			return ResponseEntity.ok(responseBody);
		}
		
		return ResponseEntity.notFound().build();		
	}
	
	private int verifyAndSign(String apiKey, SigningRequest signRequest, OutputStream os) throws GeneralSecurityException, IOException {
		Optional<AuditableCredential> credential = apiKeyToCredential(apiKey);
		if (!credential.isPresent()) {
			return HttpStatus.UNAUTHORIZED.value();			
		}
		
		Optional<SignatureKey> key = keyService.findKeyForAlias(credential.get().getAgency(), signRequest.getAlias());
		if (!key.isPresent()) {
			LOG.warn("No key found with agency: {} and alias: {}", credential.get().getAgency(), signRequest.getAlias());
			return HttpStatus.BAD_REQUEST.value();
		}
		
		byte[] decodeBase64;
		try {
			decodeBase64 = Base64.decodeBase64(signRequest.getData());
			if (decodeBase64.length == 0) {
				return HttpStatus.BAD_REQUEST.value();
			}
		} catch (IllegalArgumentException e) {
			LOG.warn(e.getMessage());
			return HttpStatus.BAD_REQUEST.value();
		}
		InputStream data = new ByteArrayInputStream(decodeBase64);
		LOG.info("Received signature request on key alias: {}", signRequest.getAlias());
		
		signingService.addWithLtv(signRequest, os, credential, key, data);
		return HttpStatus.OK.value();
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
