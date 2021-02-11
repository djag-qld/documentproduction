package au.gov.qld.bdm.documentproduction.audit;

import java.util.Date;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.entity.AuditAction;
import au.gov.qld.bdm.documentproduction.audit.entity.AuditActionRepository;
import au.gov.qld.bdm.documentproduction.audit.entity.AuditView;

@Service
public class AuditService {
	private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);
	
	private final AuditActionRepository auditActionRepository;

	@Autowired
	public AuditService(AuditActionRepository auditActionRepository) {
		this.auditActionRepository = auditActionRepository;
	}

	public void audit(AuditAction auditAction)	 {
		LOG.info("Recording audit action: {}", ToStringBuilder.reflectionToString(auditAction));
		auditActionRepository.save(auditAction);
	}

	@Transactional(Transactional.TxType.NEVER)
	public DataTablesOutput<AuditView> list(DataTablesInput input, String agency) {
		DataTablesOutput<AuditAction> all = auditActionRepository.findAll(input, (root, query, cb) -> {
			return cb.equal(root.get("agency"), agency);
		});
		DataTablesOutput<AuditView> views = new DataTablesOutput<>();
		views.setData(all.getData().stream().map(AuditService::toView).collect(Collectors.toList()));
		views.setDraw(all.getDraw());
		views.setError(all.getError());
		views.setRecordsFiltered(all.getRecordsFiltered());
		views.setRecordsTotal(all.getRecordsTotal());
		return views;
	}
	
	private static AuditView toView(AuditAction entity) {		
		return new AuditView() {
			private String targetType = entity.getTargetType();
			private String target = entity.getTarget();
			private String event = entity.getEvent();
			private String createdBy = entity.getCreatedBy();
			private Date created = new Date(entity.getCreated().getTime());
			
			@Override
			public String getTargetType() {
				return targetType;
			}
			
			@Override
			public String getTarget() {
				return target;
			}
			
			@Override
			public String getEvent() {
				return event;
			}
			
			@Override
			public String getCreatedBy() {
				return createdBy;
			}
			
			@Override
			public Date getCreated() {
				return created;
			}
		};
	}
}
