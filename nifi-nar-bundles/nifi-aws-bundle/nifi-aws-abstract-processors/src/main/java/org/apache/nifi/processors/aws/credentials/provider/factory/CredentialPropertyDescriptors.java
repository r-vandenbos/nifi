/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.aws.credentials.provider.factory;

import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.resource.ResourceCardinality;
import org.apache.nifi.components.resource.ResourceType;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.aws.AwsPropertyDescriptors;
import org.apache.nifi.ssl.SSLContextService;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.apache.nifi.processors.aws.signer.AwsSignerType.AWS_V4_SIGNER;
import static org.apache.nifi.processors.aws.signer.AwsSignerType.CUSTOM_SIGNER;
import static org.apache.nifi.processors.aws.signer.AwsSignerType.DEFAULT_SIGNER;

/**
 * Shared definitions of properties that specify various AWS credentials.
 *
 * @see <a href="http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html">
 *     Providing AWS Credentials in the AWS SDK for Java</a>
 */
public class CredentialPropertyDescriptors {

    /**
     * Specifies use of the Default Credential Provider Chain
     *
     * @see <a href="http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html#id1">
     *     AWS SDK: Default Credential Provider Chain
     *     </a>
     */
    public static final PropertyDescriptor USE_DEFAULT_CREDENTIALS = new PropertyDescriptor.Builder()
            .name("default-credentials")
            .displayName("Use Default Credentials")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .sensitive(false)
            .allowableValues("true", "false")
            .defaultValue("false")
            .description("If true, uses the Default Credential chain, including EC2 instance profiles or roles, " +
                "environment variables, default user credentials, etc.")
            .build();

    public static final PropertyDescriptor CREDENTIALS_FILE = new PropertyDescriptor.Builder()
            .name("Credentials File")
            .displayName("Credentials File")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .identifiesExternalResource(ResourceCardinality.SINGLE, ResourceType.FILE)
            .description("Path to a file containing AWS access key and secret key in properties file format.")
            .build();

    public static final PropertyDescriptor ACCESS_KEY = new PropertyDescriptor.Builder()
            .name("Access Key")
            .displayName("Access Key ID")
            .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(true)
            .build();

    public static final PropertyDescriptor SECRET_KEY = new PropertyDescriptor.Builder()
            .name("Secret Key")
            .displayName("Secret Access Key")
            .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(true)
            .build();

    /**
     * Specifies use of a named profile credential.
     *
     * @see <a href="http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/profile/ProfileCredentialsProvider.html">
     *     ProfileCredentialsProvider</a>
     */
    public static final PropertyDescriptor PROFILE_NAME = new PropertyDescriptor.Builder()
            .name("profile-name")
            .displayName("Profile Name")
            .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(false)
            .description("The AWS profile name for credentials from the profile configuration file.")
            .build();

    public static final PropertyDescriptor USE_ANONYMOUS_CREDENTIALS = new PropertyDescriptor.Builder()
            .name("anonymous-credentials")
            .displayName("Use Anonymous Credentials")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .sensitive(false)
            .allowableValues("true", "false")
            .defaultValue("false")
            .description("If true, uses Anonymous credentials")
            .build();

    /**
     * AWS Role Arn used for cross account access
     *
     * @see <a href="http://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html#genref-arns">AWS ARN</a>
     */
    public static final PropertyDescriptor ASSUME_ROLE_ARN = new PropertyDescriptor.Builder()
            .name("Assume Role ARN")
            .displayName("Assume Role ARN")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(false)
            .description("The AWS Role ARN for cross account access. This is used in conjunction with Assume Role Session Name and other Assume Role properties.")
            .build();

    /**
     * The role name while creating aws role
     */
    public static final PropertyDescriptor ASSUME_ROLE_NAME = new PropertyDescriptor.Builder()
            .name("Assume Role Session Name")
            .displayName("Assume Role Session Name")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(false)
            .description("The AWS Role Session Name for cross account access. This is used in conjunction with Assume Role ARN.")
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    /**
     * Max session time for role based credentials. The range is between 900 and 3600 seconds.
     */
    public static final PropertyDescriptor MAX_SESSION_TIME = new PropertyDescriptor.Builder()
            .name("Session Time")
            .displayName("Assume Role Session Time")
            .description("Session time for role based session (between 900 and 3600 seconds). This is used in conjunction with Assume Role ARN.")
            .defaultValue("3600")
            .required(false)
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .sensitive(false)
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    /**
     * The ExternalId used while creating aws role.
     */
    public static final PropertyDescriptor ASSUME_ROLE_EXTERNAL_ID = new PropertyDescriptor.Builder()
            .name("assume-role-external-id")
            .displayName("Assume Role External ID")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(false)
            .description("External ID for cross-account access. This is used in conjunction with Assume Role ARN.")
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    public static final PropertyDescriptor ASSUME_ROLE_SSL_CONTEXT_SERVICE = new PropertyDescriptor.Builder()
            .name("assume-role-ssl-context-service")
            .displayName("Assume Role SSL Context Service")
            .description("SSL Context Service used when connecting to the STS Endpoint.")
            .identifiesControllerService(SSLContextService.class)
            .required(false)
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    /**
     * Assume Role Proxy variables for configuring proxy to retrieve keys
     */
    public static final PropertyDescriptor ASSUME_ROLE_PROXY_HOST = new PropertyDescriptor.Builder()
            .name("assume-role-proxy-host")
            .displayName("Assume Role Proxy Host")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(false)
            .description("Proxy host for cross-account access, if needed within your environment. This will configure a proxy to request for temporary access keys into another AWS account.")
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    public static final PropertyDescriptor ASSUME_ROLE_PROXY_PORT = new PropertyDescriptor.Builder()
            .name("assume-role-proxy-port")
            .displayName("Assume Role Proxy Port")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .sensitive(false)
            .description("Proxy port for cross-account access, if needed within your environment. This will configure a proxy to request for temporary access keys into another AWS account.")
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    public static final PropertyDescriptor ASSUME_ROLE_STS_ENDPOINT = new PropertyDescriptor.Builder()
            .name("assume-role-sts-endpoint")
            .displayName("Assume Role STS Endpoint Override")
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(false)
            .description("The default AWS Security Token Service (STS) endpoint (\"sts.amazonaws.com\") works for " +
                    "all accounts that are not for China (Beijing) region or GovCloud. You only need to set " +
                    "this property to \"sts.cn-north-1.amazonaws.com.cn\" when you are requesting session credentials " +
                    "for services in China(Beijing) region or to \"sts.us-gov-west-1.amazonaws.com\" for GovCloud.")
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    public static final PropertyDescriptor ASSUME_ROLE_STS_REGION = new PropertyDescriptor.Builder()
            .name("assume-role-sts-region")
            .displayName("Assume Role STS Region")
            .description("The AWS Security Token Service (STS) region")
            .dependsOn(ASSUME_ROLE_ARN)
            .allowableValues(getAvailableRegions())
            .defaultValue(createAllowableValue(Region.US_WEST_2).getValue())
            .build();

    public static final PropertyDescriptor ASSUME_ROLE_STS_SIGNER_OVERRIDE = new PropertyDescriptor.Builder()
            .name("assume-role-sts-signer-override")
            .displayName("Assume Role STS Signer Override")
            .description("The AWS STS library uses Signature Version 4 by default. This property allows you to plug in your own custom signer implementation.")
            .required(false)
            .allowableValues(EnumSet.of(
                    DEFAULT_SIGNER,
                    AWS_V4_SIGNER,
                    CUSTOM_SIGNER))
            .defaultValue(DEFAULT_SIGNER.getValue())
            .dependsOn(ASSUME_ROLE_ARN)
            .build();

    public static final PropertyDescriptor ASSUME_ROLE_STS_CUSTOM_SIGNER_CLASS_NAME = new PropertyDescriptor.Builder()
            .fromPropertyDescriptor(AwsPropertyDescriptors.CUSTOM_SIGNER_CLASS_NAME)
            .dependsOn(ASSUME_ROLE_STS_SIGNER_OVERRIDE, CUSTOM_SIGNER)
            .build();

    public static final PropertyDescriptor ASSUME_ROLE_STS_CUSTOM_SIGNER_MODULE_LOCATION = new PropertyDescriptor.Builder()
            .fromPropertyDescriptor(AwsPropertyDescriptors.CUSTOM_SIGNER_MODULE_LOCATION)
            .dependsOn(ASSUME_ROLE_STS_SIGNER_OVERRIDE, CUSTOM_SIGNER)
            .build();

    public static AllowableValue createAllowableValue(final Region region) {
        return new AllowableValue(region.id(), region.metadata().description(), "AWS Region Code : " + region.id());
    }

    public static AllowableValue[] getAvailableRegions() {
        final List<AllowableValue> values = new ArrayList<>();
        for (final Region region : Region.regions()) {
            if (region.isGlobalRegion()) {
                continue;
            }
            values.add(createAllowableValue(region));
        }
        return values.toArray(new AllowableValue[0]);
    }
}
