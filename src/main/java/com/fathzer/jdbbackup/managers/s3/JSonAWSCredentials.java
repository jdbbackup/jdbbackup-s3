package com.fathzer.jdbbackup.managers.s3;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;

/** An AWSCredentialsProvider that reads the credentials from a json file.
 */
public class JSonAWSCredentials implements AWSCredentialsProvider {
	public static final String AWS_APPLICATION_CREDENTIALS_VAR = "AWS_APPLICATION_CREDENTIALS";
	public static final AWSCredentialsProvider INSTANCE = new JSonAWSCredentials();
	
	private static final class CoolCredentials {
		private String accessKey;
		private String secretKey;
		
		private CoolCredentials() {
			//Just to have Sonar stop asking for a constructor
		}
		
		public String getAccessKey() {
			return accessKey;
		}
		public String getSecretKey() {
			return secretKey;
		}
	}

	private Map<String, AWSCredentials> cache;
	
	protected JSonAWSCredentials() {
		this.cache = new ConcurrentHashMap<>();
	}

	@Override
	public AWSCredentials getCredentials() {
		final String env = System.getenv(AWS_APPLICATION_CREDENTIALS_VAR);
		if (env==null) {
			throw new SdkClientException(AWS_APPLICATION_CREDENTIALS_VAR+" environment variable is not set");
		}
		return cache.computeIfAbsent(env, this::read);
	}
	
	AWSCredentials read(String path) {
		try {
			return toCredentials(new ObjectMapper().readValue(new File(path), CoolCredentials.class));
		} catch (IOException e) {
			throw new SdkClientException("Unable to load AWS credentials from environment variable "+AWS_APPLICATION_CREDENTIALS_VAR, e);
		}
	}
	
	private static AWSCredentials toCredentials(CoolCredentials c) {
		try {
			return new BasicAWSCredentials(c.getAccessKey(), c.getSecretKey());
		} catch (IllegalArgumentException e) {
			throw new SdkClientException(e);
		}
	}

	@Override
	public void refresh() {
		// Do nothing
	}
}
