package au.gov.qld.bdm.documentproduction.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.gson.Gson;

@Service
public class BulkProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(BulkProcessor.class);
	
	private final String queueUrl;
	private final DocumentService documentService;
	
	@Autowired
	public BulkProcessor(@Value("${aws.sqs.queueUrl}") String queueUrl, DocumentService documentService) {
		this.queueUrl = queueUrl;
		this.documentService = documentService;
		if (StringUtils.isBlank(queueUrl)) {
			LOG.info("No queue URL. Bulk processor disabled");
		} else {
			LOG.info("Bulk processor using queue URL: {}", queueUrl);
		}
	}
	
	@Scheduled(fixedDelay = 1000)
	public void readBucketEvent() throws IOException {
		if (StringUtils.isBlank(queueUrl)) {
			return;
		}
		
		LOG.info("Looking for new messages");
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
		request.setWaitTimeSeconds(10);
		request.setMaxNumberOfMessages(10);
		ReceiveMessageResult result = sqs.receiveMessage(request);
		for (Message message : result.getMessages()) {
			S3EventNotification notification = S3EventNotification.parseJson(result.getMessages().get(0).getBody());
			if (notification != null && notification.getRecords() != null) {
				for (S3EventNotificationRecord record : notification.getRecords()) {
					if ("ObjectCreated:Put".equals(record.getEventName())) {
						processRecord(record);
					}
				}
			}

			LOG.info("Done with message");
			sqs.deleteMessage(queueUrl, message.getReceiptHandle());
		}
		
	}

	private void processRecord(S3EventNotificationRecord record) {
		String bucketName = record.getS3().getBucket().getName();
		String key = record.getS3().getObject().getKey();
		if (!key.endsWith("json")) {
			LOG.info("Ignoring object in bucket: {} with key: {}", bucketName, key);
			return;
		}
		
		LOG.info("Processing record in bucket: {} with key: {}", bucketName, key);
		AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
		try {
			if (!s3Client.doesObjectExist(bucketName, key)) {
				LOG.info("Object no longer exists in bucket: {} with key: {}", bucketName, key);
				return;
			}
			
			S3Object s3Object = s3Client.getObject(bucketName, key);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(s3Object.getObjectContent(), baos);
			
			BulkProcessingRequest request = new Gson().fromJson(baos.toString(), BulkProcessingRequest.class);
			LOG.info("Parsed bulk processing request from bucket: {} with key: {} to: {}", bucketName, key);
			
			final String documentId = documentService.record(request.getCredential(), request.getTemplateAlias(), request.getSignatureAlias());
			ByteArrayOutputStream pdfOs = new ByteArrayOutputStream();
			documentService.produce(request.getCredential(), documentId, request.getTemplateModel(), DocumentOutputFormat.PDF, pdfOs);
			
			final byte[] pdfData;
			if (request.getSignatureAlias() == null || request.getSignatureAlias().isEmpty()) {
				pdfData = pdfOs.toByteArray();
			} else {
				ByteArrayOutputStream signedOs = new ByteArrayOutputStream();
				ByteArrayInputStream pdf = new ByteArrayInputStream(pdfOs.toByteArray());
				documentService.sign(request.getCredential(), documentId, request.getTemplateModel(), pdf, signedOs);
				pdfData = signedOs.toByteArray();
			}
			
			ObjectMetadata metaData = new ObjectMetadata();
			metaData.setContentType("application/pdf");
			metaData.setContentLength(pdfData.length);
			metaData.setBucketKeyEnabled(true);
			String pdfKey = "output/" + documentId + ".pdf";
			LOG.info("Writing out document: {}", pdfKey);
			s3Client.putObject(bucketName, pdfKey, new ByteArrayInputStream(pdfData), metaData);
			moveToProcessed(bucketName, key, s3Client);
		} catch (Exception e) {
			LOG.error("Could not process bucket: {} and key: {} with error: {}", bucketName, key, e.getMessage());
			LOG.error(e.getMessage(), e);
			moveToFailed(bucketName, key, s3Client);
		}
	}

	private void moveToFailed(String bucketName, String key, AmazonS3 s3Client) {
		String destinationKey = "failed/" + key;
		LOG.info("Copying from bucket: {} with key: {} to: {}", bucketName, key, destinationKey);
		s3Client.copyObject(bucketName, key, bucketName, destinationKey);
		LOG.info("Removing from bucket: {} with key: {}", bucketName, key);
		s3Client.deleteObject(bucketName, key);
	}

	private void moveToProcessed(String bucketName, String key, AmazonS3 s3Client) {
		String destinationKey = "processed/" + key;
		LOG.info("Copying from bucket: {} with key: {} to: {}", bucketName, key, destinationKey);
		s3Client.copyObject(bucketName, key, bucketName, destinationKey);
		LOG.info("Removing from bucket: {} with key: {}", bucketName, key);
		s3Client.deleteObject(bucketName, key);
	}
	
}
