package au.gov.qld.bdm.documentproduction.document;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.gson.Gson;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.util.AwsHelper;

@RunWith(MockitoJUnitRunner.class)
public class BulkProcessorTest {

	private static final String QUEUE_URL = "some queue url";
	private static final String RECEIPT_HANDLE = "some receipt handle";
	private static final String DOCUMENT_ID = "some document id";
	private static final String TEMPLATE = "some template";
	
	@Mock DocumentService documentService;
	@Mock AwsHelper awsHelper;
	@Mock AmazonSQS sqs;
	@Mock ReceiveMessageResult messageResult;
	@Mock Message message;
	@Mock AmazonS3 s3;
	
	BulkProcessor processor;
	String s3MessageNotification;
	
	@Before
	public void setUp() throws Exception {
		s3MessageNotification = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("s3-event.json"), StandardCharsets.UTF_8);
		when(awsHelper.getS3Client()).thenReturn(s3);
		when(awsHelper.getSqsClient()).thenReturn(sqs);
		when(sqs.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(messageResult);
		processor = new BulkProcessor(QUEUE_URL, documentService, awsHelper);
	}
	
	@Test
	public void shouldWriteMetadataAndPdfToOutput() throws Exception {
		BulkProcessingRequest bulkProcessingRequest = new BulkProcessingRequest();
		bulkProcessingRequest.setTemplateAlias(TEMPLATE);
		bulkProcessingRequest.setSignatureAlias(Collections.emptyList());
		when(documentService.record(isA(AuditableCredential.class), eq(TEMPLATE), eq(Collections.emptyList()))).thenReturn(DOCUMENT_ID);
		
		when(s3.doesObjectExist("testbucket", "testdata.json")).thenReturn(true);
		S3Object s3Object = new S3Object();
		s3Object.setKey("testdata.json");
		s3Object.setObjectContent(new ByteArrayInputStream(new Gson().toJson(bulkProcessingRequest).getBytes(StandardCharsets.UTF_8)));
		when(s3.getObject("testbucket", "testdata.json")).thenReturn(s3Object);
		when(message.getReceiptHandle()).thenReturn(RECEIPT_HANDLE);
		when(messageResult.getMessages()).thenReturn(asList(message));
		when(message.getBody()).thenReturn(s3MessageNotification);
		
		processor.readBucketEvent();
		verify(s3).putObject(eq("testbucket"), eq("output/" + DOCUMENT_ID + ".pdf"), isA(ByteArrayInputStream.class), isA(ObjectMetadata.class));
		verify(s3).putObject(eq("testbucket"), eq("output/testdata.json"), contains(DOCUMENT_ID));
		verify(sqs).deleteMessage(QUEUE_URL, RECEIPT_HANDLE);
	}
	
	@Test
	public void shouldMoveFileToFailedIfCannotRead() throws Exception {
		BulkProcessingRequest bulkProcessingRequest = new BulkProcessingRequest();
		bulkProcessingRequest.setTemplateAlias(TEMPLATE);
		bulkProcessingRequest.setSignatureAlias(Collections.emptyList());
		bulkProcessingRequest.setTemplateModel(new HashMap<>());
		when(documentService.record(isA(AuditableCredential.class), eq(TEMPLATE), eq(Collections.emptyList()))).thenReturn(DOCUMENT_ID);
		doThrow(new RuntimeException("expected")).when(documentService).produce(isA(AuditableCredential.class), eq(DOCUMENT_ID), isA(Map.class), eq(DocumentOutputFormat.PDF), isA(OutputStream.class));
		
		when(s3.doesObjectExist("testbucket", "testdata.json")).thenReturn(true);
		S3Object s3Object = new S3Object();
		s3Object.setKey("testdata.json");
		s3Object.setObjectContent(new ByteArrayInputStream(new Gson().toJson(bulkProcessingRequest).getBytes(StandardCharsets.UTF_8)));
		when(s3.getObject("testbucket", "testdata.json")).thenReturn(s3Object);
		when(message.getReceiptHandle()).thenReturn(RECEIPT_HANDLE);
		when(messageResult.getMessages()).thenReturn(asList(message));
		when(message.getBody()).thenReturn(s3MessageNotification);
		
		processor.readBucketEvent();
		verify(s3, never()).putObject(eq("testbucket"), eq("output/" + DOCUMENT_ID + ".pdf"), isA(ByteArrayInputStream.class), isA(ObjectMetadata.class));
		verify(s3).copyObject(eq("testbucket"), eq("testdata.json"), eq("testbucket"), eq("failed/testdata.json"));
		verify(sqs).deleteMessage(QUEUE_URL, RECEIPT_HANDLE);
	}
}
