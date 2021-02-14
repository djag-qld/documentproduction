# Document Production
This service is used to sign PDFs with digital signatures. The digital signatures prevent the PDF from being modified without subsequently invalidating the signature since the signature is based off the content of the PDF itself. This service also supports generating PDFs based on templates and provided variables to pre-populate them.

## Key features
1. Web administration portal integrated with CAS https://github.com/apereo/cas for authentication
2. API key management
3. Template management and processing with FreeMarker https://github.com/apache/freemarker
4. Signature and key management, including provisioning of CSRs for AATL members to sign
5. Auditing of changes and document production

## Architecture
![Architecture](https://github.com/qld-gov-au/documentproduction/raw/main/arch.png)

### Technology stack
| Application | Infrastructure |
| ------------- |-------------|
| Java 8 | AWS set up by Cloudformation |
| Spring Boot | IAM controls for access to KMS and Secrets Manager by application / instances | 
| REST web services via Spring REST controllers and Swagger UI | PostgreSQL on RDS |
| PDFBox and BouncyCastle for signing | Beanstalk application environment |
| FreeMarker templating | KMS for signing |
| | Secrets Manager for RDS access |

Administration portal UI built upon the Colorlib Gentelella theme: https://github.com/ColorlibHQ/gentelella

## Installation guide
### Prerequisites
- AWS account with sufficient permission to create resources including IAM entries.
- WAF WebACL rules suitable for you environment. Note: You will need to allow sufficiently large payloads to be submitted for sending PDFs to be signed.
- IAM policy managed separately for you to allocate KMS signing permissions as you create KMS keys. It should similar to:
```{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ReadSecrets",
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue",
                "secretsmanager:DescribeSecret"
            ],
            "Resource": "arn:aws:secretsmanager:REGION:ACCOUNT_ID:secret:ENV*/databases/documentproduction-*"
        },
        {
            "Sid": "ReadKms",
            "Effect": "Allow",
            "Action": [
                "kms:Sign",
                "kms:GetPublicKey"
            ],
            "Resource": "arn:aws:kms:REGION:ACCOUNT_ID:key/KMS_KEY_ID"
        }
    ]
}
```
- Maven and at least Java 8 to build the application.
- Running CAS which provides CAS v2 protocol and attributes for role and agency. The role is checked against for basic administration access (see Cloudformation property referencing what you want the role to be called). The agency is used to separate all data so that a running instance can support multiple agencies/business units.

Note: The infrastructure is handled by a Cloudformation template under aws/cf. 

### Steps
1. Create an S3 bucket which you will store your build artifacts in. Cloudformation will use these files to create the application and Lambda environment information trigger.
2. Copy the LoadBalancer.zip or build it yourself and upload it to your S3 build artifacts bucket.
3. Build the application using Maven. You may want to make your own copy of the app/src/env/awstest and resources within for your specific need. The app/src/env/awstest/application.properties relies on environment properties provided by Cloudformation.
4. Upload the built JAR file to your S3 build artifacts bucket.
5. Deploy the cf.json template and provide necessary properties/tags.
```
aws cloudformation create-stack --stack-name testdocumentproduction --template-body file://cf.json --tags file://testtags.json --parameters file://test.json --capabilities CAPABILITY_IAM
```

## Administration
### Creating an API key
1. Login to the administration portal
2. Choose API Keys in the navigation panel
3. Take a copy of the API key automatically generated - this value cannot be recovered once you submit the Create key request. The whole key’s value is needed for API requests
4. Click the submit button
5. Your API key should be shown in the table above, identified by the prefix characters

### Creating a signing key
1. Create a key in KMS for signing
2. Update the IAM policy for reading secrets to allow signing access to the application
3. Login to the administration portal
4. Choose Keys in the navigation panel
5. Save a new key with an alias (referred to for any updates and signing requests)
6. Enter the KMS ID (it should look like a UUID) from AWS
7. Optionally enter a Timestamp endpoint. The timestamp service should be provided by your certificate authority and is required for LTV.
8. Leave the certificate response blank
9. Press Save - the key should appear in the list above
10. Create a Certificate Signing Request (CSR) by choosing the signing key from the drop-down (make sure to pick the highest version visible for your alias)
11. Enter a Subject DN in the format: CN=Agency,OU=Your department name,O=Usually whole of government name (e.g. Queensland Government),L=Your city/town/locality,ST=State/Territory/etc,C=Two character country code (e.g. AU). Note: If you have commas in a field, such as CN, you need to escape them.
12. Click Create - a CSR should download
13. Provide your CSR to someone on the AATL and/or your department authority
14. Once you receive a signed certificate, enter all the same fields into the Save Key form including your certificate this time. Note: The administration service is a little hard to use at this point and saves over everything based on the alias to create a new version.
