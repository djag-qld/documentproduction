#  Copyright 2016 Amazon Web Services, Inc. or its affiliates. All Rights Reserved.
#  This file is licensed to you under the AWS Customer Agreement (the "License").
#  You may not use this file except in compliance with the License.
#  A copy of the License is located at http://aws.amazon.com/agreement/ .
#  This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied.
#  See the License for the specific language governing permissions and limitations under the License.

import requests
import json

SUCCESS = "SUCCESS"
FAILED = "FAILED"

def send(event, context, responseStatus, responseData, physicalResourceId=None, noEcho=False):
    responseUrl = event['ResponseURL']

    print(responseUrl)

    responseBody = {}
    responseBody['Status'] = responseStatus
    responseBody['Reason'] = 'See the details in CloudWatch Log Stream: ' + context.log_stream_name
    responseBody['PhysicalResourceId'] = physicalResourceId or context.log_stream_name
    responseBody['StackId'] = event['StackId']
    responseBody['RequestId'] = event['RequestId']
    responseBody['LogicalResourceId'] = event['LogicalResourceId']
    responseBody['NoEcho'] = noEcho
    responseBody['Data'] = responseData

    json_responseBody = json.dumps(responseBody)

    print("Response body:\n" + json_responseBody)

    headers = {
        'content-type' : '',
        'content-length' : str(len(json_responseBody))
    }

    try:
        response = requests.put(responseUrl,
                                data=json_responseBody,
                                headers=headers)
        print("Status code: " + response.reason)
    except Exception as e:
        print("send(..) failed executing requests.put(..): " + str(e))



# Retrieves the ALB ARN for use by the WAF association - with huge thanks to Marcin on StackOverflow:
# https://stackoverflow.com/questions/65677754/awswafv2webaclassociation-resourcearn-for-application-load-balancer-in-cloud

import json
import logging
import boto3

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):
    logger.info('got event {}'.format(event))
    try:
        eb = boto3.client('elasticbeanstalk')
        responseData = {}
        if event['RequestType'] in ["Create", "Update"]:
            eb_env_name = event['ResourceProperties']['EBEnvName']
            response = eb.describe_environment_resources(
                EnvironmentName=eb_env_name
            )

            lb_arn = response['EnvironmentResources']['LoadBalancers'][0]['Name']
            logger.info(str(response['EnvironmentResources']['LoadBalancers'][0]['Name']))

            responseData = {
                "LBArn": lb_arn
            }

            send(event, context, SUCCESS, responseData)
        else:
            logger.info('Unexpected RequestType!') 
            send(event, context, 
            SUCCESS, responseData)
    except Exception as err:
        logger.error(err)
        responseData = {"Data": str(err)}
        send(event, context, FAILED, responseData)
    return

