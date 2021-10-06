package au.gov.qld.bdm.documentproduction.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.google.common.collect.ImmutableMap;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.document.entity.Document;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentCounter;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentCounterRepository;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentRepository;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentSignature;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentSignatureView;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentView;
import au.gov.qld.bdm.documentproduction.sign.ContentSignerFactory;
import au.gov.qld.bdm.documentproduction.sign.SigningService;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKeyView;
import au.gov.qld.bdm.documentproduction.template.TemplateService;
import au.gov.qld.bdm.documentproduction.template.entity.Template;
import au.gov.qld.bdm.documentproduction.template.entity.TemplateView;
import freemarker.template.TemplateException;

@Service
public class DocumentService {
	private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);
	private final DocumentRepository repository;
	private final DocumentSignatureService documentSignatureService;
	private final AuditService auditService;
	private final TemplateService templateService;
	private static final float DPI = 300f;
	private static final float A4_RATIO = 4f / 3f;
	private static final float DOTS_PER_POINT = DPI * A4_RATIO;
	private static final int DPP = 96;
	private static final String TEMPLATE_WRAPPER_FMT = "<#compress><?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">%s</#compress>";
	private final InlineTemplateService inlineTemplateService;
	private final SigningService signingService;
	private final DocumentCounterRepository documentCounterRepository;
	private final Resource[] classpathFonts;
	private final SignatureKeyService signatureKeyService;
	private final ContentSignerFactory contentSignerFactory;
	private final SignatureRecordService signatureRecordService;
	
	private List<Resource> fileSystemFonts;
	
	@Autowired
	public DocumentService(DocumentRepository repository, DocumentSignatureService documentSignatureService, TemplateService templateService, SigningService signingService,
			AuditService auditService, InlineTemplateService inlineTemplateService, @Value("classpath:fonts/*.ttf") Resource[] classpathFonts, 
			DocumentCounterRepository documentCounterRepository, SignatureKeyService signatureKeyService, ContentSignerFactory contentSignerFactory,
			SignatureRecordService signatureRecordService) throws IOException {
		this.repository = repository;
		this.documentSignatureService = documentSignatureService;
		this.templateService = templateService;
		this.signingService = signingService;
		this.auditService = auditService;
		this.inlineTemplateService = inlineTemplateService;
		this.documentCounterRepository = documentCounterRepository;
		this.classpathFonts = classpathFonts;
		this.signatureKeyService = signatureKeyService;
		this.contentSignerFactory = contentSignerFactory;
		this.signatureRecordService = signatureRecordService;
		this.fileSystemFonts = saveFontsToFileSystem();
		verifyFontsExist(fileSystemFonts);
	}

	private List<Resource> saveFontsToFileSystem() throws FileNotFoundException, IOException {
		List<Resource> fileFontResources = new ArrayList<>();
		for (Resource font : classpathFonts) {
			File tempFile = new File(FileUtils.getTempDirectoryPath() + File.separator + font.getFilename());
			FileOutputStream fileWriter = new FileOutputStream(tempFile);
			IOUtils.copy(font.getInputStream(), fileWriter);
			font.getInputStream().close();
			fileWriter.flush();
			fileWriter.close();
			fileFontResources.add(new FileSystemResource(tempFile));
		}
		LOG.info("Loaded fonts: {}", fileFontResources);
		return fileFontResources;
	}
	
	public DataTablesOutput<DocumentView> list(DataTablesInput input, String agency) {
		DataTablesOutput<Document> all = repository.findAll(input, (root, query, cb) -> {
			return cb.equal(root.get("agency"), agency);
		});
		DataTablesOutput<DocumentView> views = new DataTablesOutput<>();
		views.setData(all.getData().stream().map(DocumentService::toView).collect(Collectors.toList()));
		views.setDraw(all.getDraw());
		views.setError(all.getError());
		views.setRecordsFiltered(all.getRecordsFiltered());
		views.setRecordsTotal(all.getRecordsTotal());
		return views;
	}
	
	private static DocumentView toView(Document entity) {		
		return new DocumentView() {
			private final String createdBy = entity.getCreatedBy();
			private final Date created = new Date(entity.getCreated().getTime());
			private final String templateAlias = entity.getTemplate().getAlias();
			private final Collection<DocumentSignatureView> signatures = entity.getSignatures().stream().map(s -> {
				return new DocumentSignatureView() {
					@Override
					public String getAlias() {
						return s.getAlias();
					}

					@Override
					public String getCreatedBy() {
						return null;
					}

					@Override
					public Date getCreated() {
						return null;
					}

					@Override
					public String getReasonTemplate() {
						return null;
					}

					@Override
					public String getSignatoryTemplate() {
						return null;
					}

					@Override
					public SignatureKeyView getSignatureKey() {
						return new SignatureKeyView() {
							@Override
							public String getAlias() {
								return s.getSignatureKey().getAlias();
							}

							@Override
							public int getVersion() {
								return s.getSignatureKey().getVersion();
							}

							@Override
							public String getCreatedBy() {
								return s.getSignatureKey().getCreatedBy();
							}

							@Override
							public Date getCreated() {
								return s.getSignatureKey().getCreated();
							}

							@Override
							public String getTimestampEndpoint() {
								return s.getSignatureKey().getTimestampEndpoint();
							}
						};
					}

					@Override
					public String getLocationTemplate() {
						return null;
					}

					@Override
					public String getContactInfoTemplate() {
						return null;
					}
				};
			}).collect(Collectors.toList());
			
			@Override
			public String getCreatedBy() {
				return createdBy;
			}
			
			@Override
			public Date getCreated() {
				return created;
			}

			@Override
			public TemplateView getTemplate() {
				return new TemplateView() {
					@Override
					public String getAlias() {
						return templateAlias;
					}

					@Override
					public String getCreatedBy() {
						return null;
					}

					@Override
					public Date getCreated() {
						return null;
					}

					@Override
					public String getContent() {
						return null;
					}
				};
			}

			@Override
			public Collection<DocumentSignatureView> getSignatures() {
				return signatures;
			}

			@Override
			public String getId() {
				return entity.getId();
			}

			@Override
			public long getCounter() {
				return entity.getCounter();
			}
		};
	}

	/**
	 * Record a document request against an agency.
	 * 
	 * @param credential - typically an API key validated and documents created against its agency.
	 * @param templateAlias - refers to a Template in the credential's agency by its alias. uses the latest template with that agency+alias.
	 * @param signatureAlias - list of signature aliases to apply to the document when produced
	 * @return created document ID
	 */
	public String record(AuditableCredential credential, String templateAlias, List<String> signaturesAlias) {
		List<DocumentSignature> signatures = signaturesAlias.stream().map(alias -> {
			Optional<DocumentSignature> findByAliasAndAgency = documentSignatureService.findByAliasAndAgency(alias, credential.getAgency());
			if (!findByAliasAndAgency.isPresent()) {
				throw new IllegalArgumentException("No signature found by alias: " + alias + " for agency: " + credential.getAgency());
			}
			return findByAliasAndAgency.get();
		}).collect(Collectors.toList());
		
		Optional<Template> template = templateService.findByAliasAndAgency(templateAlias, credential.getAgency());
		if (!template.isPresent()) {
			throw new IllegalArgumentException("No template found by alias: " + templateAlias + " for agency: " + credential.getAgency());
		}

		Document document = new Document(credential.getId());
		auditService.audit(AuditBuilder.of("record").from(credential).target(template.get().getId(), document.getId(), "document").build());
		
		document.setAgency(credential.getAgency());
		document.setSignatures(signatures);
		document.setTemplate(template.get());
		document.setCounter(documentCounterRepository.save(new DocumentCounter()).getCounter());
		return repository.save(document).getId();
	}

	public void produce(AuditableCredential credential, String documentId, Map<String, String> templateModel, DocumentOutputFormat outputFormat, OutputStream os) {
		Optional<Document> byId = repository.findById(documentId);
		if (!byId.isPresent() || !byId.get().getAgency().equals(credential.getAgency())) {
			throw new IllegalArgumentException("Could not find document with that ID and for that agency");
		}

		try {
			auditService.audit(AuditBuilder.of("produce").from(credential).target(byId.get().getId(), byId.get().getId(), "document").build());
			createPdf(byId.get(), templateModel, os);
		} catch (DocumentException | IOException | TemplateException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	private void createPdf(Document document, Map<String, String> templateModel, OutputStream os) throws DocumentException, IOException, TemplateException {
		Map<String, Object> formData = templateData(document, templateModel);
		LOG.info("Creating template of PDF for document: {}", document.getId());
		String templated = inlineTemplateService.template(document.getTemplate().getId(), String.format(TEMPLATE_WRAPPER_FMT, document.getTemplate().getContent()), formData);

		ITextRenderer renderer = new ITextRenderer(DOTS_PER_POINT, DPP);
		verifyFontsExist(fileSystemFonts);
		for (Resource font : fileSystemFonts) {
			renderer.getFontResolver().addFont(font.getFile().getPath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		}
		renderer.setDocumentFromString(templated);
		renderer.getSharedContext().setReplacedElementFactory(new BarcodeElementFactory(renderer.getOutputDevice(), document, templateModel, signatureKeyService,
				contentSignerFactory, signatureRecordService));
		renderer.layout();
		try {
			renderer.createPDF(os);
			renderer.finishPDF();
			os.close();
		} catch (DocumentException | IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private void verifyFontsExist(List<Resource> fonts) throws IOException {
		if (classpathFonts.length == 0) {
			LOG.debug("No fonts to save to disk from classpath");
			return;
		}
		
		if (fonts.stream().anyMatch(not(Resource::exists))) {
			saveFontsToFileSystem();
		}
		
		if (fonts.stream().anyMatch(not(Resource::exists))) {
			throw new IllegalStateException("Could not find fonts saved to file system");
		}
	}
	
	// not available until java 11
	private static <T> Predicate<T> not(Predicate<T> t) {
	    return t.negate();
	}

	private Map<String, Object> templateData(Document document, Map<String, String> templateModel) {
		Map<String, Object> formData = new HashMap<>(ImmutableMap.of("created", document.getCreated(), "lastmodified", document.getLastModified(), "documentCounter", document.getCounter()));
		formData.putAll(ImmutableMap.of("simpleData", templateModel, "templateModel", templateModel));
		return formData;
	}

	public void sign(AuditableCredential credential, String documentId, Map<String, String> templateModel, ByteArrayInputStream pdf, OutputStream signedOs) throws IOException, TemplateException, GeneralSecurityException {
		Optional<Document> byId = repository.findById(documentId);
		if (!byId.isPresent() || !byId.get().getAgency().equals(credential.getAgency())) {
			throw new IllegalArgumentException("Could not find document with that ID and for that agency");
		}

		PDDocument doc = PDDocument.load(pdf);
		for (DocumentSignature signature : byId.get().getSignatures()) {
			SignatureKey signatureKey = signature.getSignatureKey();
			String signatory = inlineTemplateService.template(signature.getId() + "Signatory", signature.getSignatoryTemplate(), templateData(byId.get(), templateModel));
			String reason = inlineTemplateService.template(signature.getId() + "Reason", signature.getReasonTemplate(), templateData(byId.get(), templateModel));
			String location = inlineTemplateService.template(signature.getId() + "Location", signature.getLocationTemplate(), templateData(byId.get(), templateModel));
			String contactInfo = inlineTemplateService.template(signature.getId() + "ContactInfo", signature.getContactInfoTemplate(), templateData(byId.get(), templateModel));
			signingService.signPdf(doc, signatureKey, signatory, reason, location, contactInfo, credential);
		}
		
		ByteArrayOutputStream tempSigned = new ByteArrayOutputStream();
		doc.saveIncremental(tempSigned);
		doc.close();

		signingService.addLtv(new ByteArrayInputStream(tempSigned.toByteArray()), signedOs);
	}
	
	public void preview(AuditableCredential credential, String templateAlias, int version, OutputStream os) throws DocumentException, IOException, TemplateException {
		Optional<Template> template = templateService.findByAliasAndAgencyAndVersion(templateAlias, credential.getAgency(), version);
		if (!template.isPresent()) {
			throw new IllegalArgumentException("No template found by alias: " + templateAlias + " for agency: " + credential.getAgency());
		}

		auditService.audit(AuditBuilder.of("preview").from(credential).target(template.get().getId(), templateAlias, "template").build());
		Document document = new Document(credential.getId());
		document.setAgency(credential.getAgency());
		document.setTemplate(template.get());
		document.setCounter(documentCounterRepository.save(new DocumentCounter()).getCounter());
		createPdf(document, Collections.emptyMap(), os);
	}
}
