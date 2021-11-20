package au.gov.qld.bdm.documentproduction.document;

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
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

@Service
public class BulkProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(BulkProcessor.class);
	
	private final String queueUrl;
	
	@Autowired
	public BulkProcessor(@Value("${aws.sqs.queueUrl}") String queueUrl) {
		this.queueUrl = queueUrl; 
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
		request.setMaxNumberOfMessages(1);
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

	private void processRecord(S3EventNotificationRecord record) throws IOException {
		String bucketName = record.getS3().getBucket().getName();
		String key = record.getS3().getObject().getKey();
		
		LOG.info("Process record in bucket: {} with key: {}", bucketName, key);
		AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
		
		S3Object s3Object = s3Client.getObject(bucketName, key);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(s3Object.getObjectContent(), baos);
		
		String destinationKey = "processed/" + key;
		LOG.info("Copying from bucket: {} with key: {} to: {}", bucketName, key, destinationKey);
		s3Client.copyObject(bucketName, key, bucketName, destinationKey);
		
		LOG.info("Removing from bucket: {} with key: {}", bucketName, key);
		s3Client.deleteObject(bucketName, key);
	}
	
}
