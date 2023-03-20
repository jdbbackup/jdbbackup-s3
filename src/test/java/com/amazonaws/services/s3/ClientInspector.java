package com.amazonaws.services.s3;

import com.amazonaws.auth.AWSCredentialsProviderChain;

public class ClientInspector {
	public static AWSCredentialsProviderChain getCredentialsProviderChain(AmazonS3Client client) {
		return (AWSCredentialsProviderChain) client.awsCredentialsProvider;
	}
	
	public static Class<?> getDefaultProviderClass() {
		return S3CredentialsProviderChain.class;
	}
}
