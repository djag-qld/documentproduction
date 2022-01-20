package au.gov.qld.bdm.documentproduction.util;

import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Service
public class AwsHelper {

	public AmazonSQS getSqsClient() {
		return AmazonSQSClientBuilder.defaultClient();
	}

	public AmazonS3 getS3Client() {
		return AmazonS3ClientBuilder.defaultClient();
	}

}
