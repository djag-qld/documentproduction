# Document Production
[![app-build](https://github.com/qld-gov-au/documentproduction/actions/workflows/java.yml/badge.svg)](https://github.com/qld-gov-au/documentproduction/actions/workflows/java.yml)
[![demo-client-build](https://github.com/qld-gov-au/documentproduction/actions/workflows/nodejs.yml/badge.svg)](https://github.com/qld-gov-au/documentproduction/actions/workflows/nodejs.yml)

This service is used to sign PDFs with digital signatures.
The service also produces documents from templates which can include signed QR codes that are used to validate the integrity of digital and printed documents.

The digital signatures prevent the PDF from being modified without subsequently invalidating the signature and QR code since the signature is based off the content of the PDF and template data.

This service is designed to be reusable through agency data segregation.

## Key features
* PDF production including barcodes and signing
* Signed QR codes for verification on screen or on printed documents
* Add LTV to the document and signatures by providing a timestamp service link (RFC3161)
* Signature and key management, including provisioning of CSRs for AATL members to sign
* API for document and signature production
* Web administration portal integrated with CAS https://github.com/apereo/cas for authentication
* Template management and processing with FreeMarker https://github.com/apache/freemarker
* Auditing of administration access, changes, signatures applied and documents produced

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
| | S3 to send bulk request files and store PDF output within |
| | SQS triggering bulk processing of request files in S3 |

Administration portal UI built upon the Colorlib Gentelella theme: https://github.com/ColorlibHQ/gentelella

## Installation guide
### Prerequisites
- AWS account with sufficient permission to create resources including IAM entries.
- WAF WebACL rules suitable for you environment. Note: You will need to allow sufficiently large payloads to be submitted for sending PDFs to be signed.
- At least one customer managed Asymmetric KMS key for signing and verifying.
- IAM policy managed separately for you to allocate KMS signing permissions as you create KMS keys. It should similar to:
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ReadKms",
            "Effect": "Allow",
            "Action": [
                "kms:Sign",
                "kms:GetPublicKey"
            ],
            "Resource": "arn:aws:kms:REGION:ACCOUNT_ID:key/*" (make sure to set your own KMS key IDs here instead)
        }
    ]
}
```
- An RDS subnet group in your preferred VPC that includes your subnets and availability zones.
- Maven and at least Java 8 to build the application.
- Running CAS which provides CAS v2 protocol and attributes for role and agency. The role is checked against for basic administration access (see Cloudformation property referencing what you want the role to be called). The agency is used to separate all data so that a running instance can support multiple agencies/business units.

Note: This service does not create KMS keys for you. You need to create them yourself and update the IAM managed policy to provide access to this service to sign and get public keys from.

### Installation Steps
1. Create an S3 bucket which you will store your build artifacts in. Cloudformation will use these files to create the application and Lambda environment information trigger.
2. Copy the LoadBalancer.zip or build it yourself and upload it to your S3 build artifacts bucket. There is a package.sh script included to help with this.
3. Build the application using Maven and your own profile or use the awstest profile to use environment variables provided by CloudFormation and Beanstalk. Otherwise, you may want to make your own copy of the app/src/env/awstest and resources within for your specific need.
4. Upload the built JAR file to your S3 build artifacts bucket.
5. Deploy the cf.json template and provide necessary properties/tags.
```
# From aws/cf/ - create your own tags and param json files to populate the template. Then you can create the stack with these properties, for example:
aws cloudformation create-stack --stack-name testdocumentproduction --template-body file://cf.json --tags file://testtags.json --parameters file://test.json --capabilities CAPABILITY_IAM
```

## Post installation administration
### Creating an API key
1. Login to the administration portal using your CAS
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

## Uninstalling
1. Delete the Cloudformation stack 
2. You may wish to delete the RDS snapshot and Cloudwatch logs
3. Clean up your KMS key and IAM managed policy

## Producing documents
The Document Production service can create and track documents produced the Documents page. Documents are produced by calling the API endpoints /api/document and /api/document/object depending on your preferred output format.
### Document templates
Document templates are FreeMarker templates that use the supplied templateModel to form the contents. The contents are then converted to a PDF by using the FlyingSaucer library and must meet the requirements of this engine. i.e. The templates must be HTML and support up to CSS 2.0.
Document templates are created with:
1. Alias (required): The alias used to refer to the most recent (and active) document template in API calls.
2. Content (required): A FreeMarker template that is provided the templateModel. Must be strict HTML and supports CSS 2.0 inline stylesheets.

Barcodes can also be created inside the PDFs by using special image tags.
```
<img src="..." type="..." qrpixels="..." imagetype="..." width="..." height="..." />
```
Use the "type" attribute with value: "qrcode" for a QR code:
| type | Description | Attribute | Values | Required |
| :---: | :---: | :---: | :---: | :---: |
| barcode | Line barcode 128 | src | Data to encode as barcode. E.g. using template data: "${templateModel['afield']}" | Yes |
| | | width | Scaled width in pixels | Yes |
| | | height | Scaled width in pixels | Yes |
| qrcode | Matrix barcode | src | Data to encode as barcode. E.g. using template data: "${templateModel['afield']}" | Yes |
| | | width | Scaled width in pixels. Make sure width and height are the same for QR codes. | Yes |
| | | height | Scaled width in pixels | Yes |
| | | imagetype | QR Code image type to allow lossy or lossless formats. Default value "png". Accepts "bmp", "jpg", "png" and "gif". | No |
| | | qrpixels | Pixels allocated to the barcode. Expected data size should determine the size. Default "125". Maximum "3000". | No |
| signedqrcode | Matrix barcode | src | Signature key alias | Yes |
| | | width | Scaled width in pixels. Make sure width and height are the same for QR codes. | Yes |
| | | height | Scaled width in pixels | Yes |
| | | imagetype | QR Code image type to allow lossy or lossless formats. Default value "png". Accepts "bmp", "jpg", "png" and "gif". | No |
| | | qrpixels | Pixels allocated to the barcode. Expected data size should determine the size. Default "125". Maximum "3000". | No |

Example of a signed QR code:
```
<img src="(Signature key alias)" type="signedqrcode" imagetype="png" qrpixels="500" width="250" height="250" />
```

Example of a line barcode 128:
```
<img src="${templateModel['afield']}" type="barcode" width="250" height="250" />
```

### Signature templates
Signature templates are used to structure signatures applied to signed PDF documents and use the supplied templateModel by using the FreeMarker engine. Whenever a signed PDF document is produced, a signature template is interpretted along with the templateModel to complete the components:
1. Signatory template (required): Template used for the title of the signature. e.g. "Signed by John Smith" could be templated as "Signed by ${templateModel['fullname']}"
2. Reason template (required): Template used for the reason for signing. e.g. "Fulfillment of marriage proceedings between ${templateModel['bride']} and ${templateModel['groom']}"
3. Location (optional): Where the signature took place. This could be a place of business, where an event occurred such as a place of marriage, etc. 
4. Contact information template (optional): Used to identify what the primary contact is for confirming a signature's legitmacy.
5. Signature key (required): Which Signing key should be used for the digital signature.

### Bulk production
The Document Production service supports bulk processing of requests. Each request is represented as a JSON object and uploaded to the S3 bucket created in CloudFormation. Each request is processed sequentially and written to the S3 bucket's output directory. 

Upload these request JSON files to the S3 bucket defined in the Cloudformation template paramter: BulkProcessingBucketName. Bulk requests must extend .json and be in the format:
```
{
  "agency": "agency which has the signature alias and template alias",
  "signatureAlias": [ "test alias" ],
  "templateAlias": "test template alias",
  "templateModel": {
    "fielda": "some field a's value",
    "fieldb": "some field b's value"
  }
}
```
Note: signatureAlias is optional and can be an empty list ("signatureAlias": []) to not have these documents signed.

The resulting documents will be written to the bucket in the format: output/documentid.pdf
If there's an error processing the request, it will be moved to the failed/ directory.

## Verifying signed QR codes in documents
Check out the web based demonstration client under client/demo in this repository.

Alternatively, you can verify from the command line using OpenSSL as well.
1. Download certificate to verify signatures.
2. Convert x509 certificate to PEM to be used by openssl verification.
```
openssl x509 -pubkey -in test.cer -outform PEM -out pub.pem
```
3. Extract the signature from the QR code and save to a file (e.g. testqr.txt).
4. Convert QR code content to binary.
The QR content is in the structure:
- All Base45 encoded.
- Contents are compressed using ZLIB.
- Metadata and the fields that were signed (f).
- A Base45 encoded signature (sig).
These steps first convert the QR content from Base45, then decompress it, then extract out the signature to be decoded into a binary form used for verification:
```
cat testqr.txt | base45 --decode | zlib-flate -uncompress | jq -r ".sig" | base45 --decode > sig
```
5. Extact all fields except the signature from the QR code to verify against the signature file created above (ensuring no trailing new line characters):
```
cat testqr.txt | base45 --decode | zlib-flate -uncompress | jq -r --compact-output "del(.sig)" | tr -d '\n' > data
```
The signature is created against the JSON format of all fields and metadata about the document. As a result, your data file should be a JSON file whose keys are in in the same order. e.g.
```
{"f":{"templatevariable":"a value"},"dId":"uuid of document","ver":"1.1.0","cdate":"2021-10-08","kid":"keyalias:keyversion"}
```
6. Verify the field data signature:
```
openssl dgst -sha256 -verify pub.pem -signature sig data
```
You should get a message: Verified OK

