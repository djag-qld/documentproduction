package au.gov.qld.bdm.documentproduction.document;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import au.gov.qld.bdm.documentproduction.document.entity.Document;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentSignatureView;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentView;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKeyView;
import au.gov.qld.bdm.documentproduction.template.entity.TemplateView;

public class DataTableDocumentView implements DocumentView {
	
	private final String createdBy;
	private final Date created;
	private final String templateAlias;
	private final Collection<DocumentSignatureView> signatures;
	private final String id;
	private final long counter;
	
	private DataTableDocumentView(Document entity) {
		this.id = entity.getId();
		this.counter = entity.getCounter();
		this.createdBy = entity.getCreatedBy();
		this.created = new Date(entity.getCreated().getTime());
		this.templateAlias = entity.getTemplate().getAlias();
		this.signatures = entity.getSignatures().stream().map(s -> {
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
	}

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

			@Override
			public int getVersion() {
				return 0;
			}
		};
	}

	@Override
	public Collection<DocumentSignatureView> getSignatures() {
		return signatures;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public long getCounter() {
		return this.counter;
	}
	
	public static DataTableDocumentView fromEntity(Document document) {
		return new DataTableDocumentView(document);
	}
}
