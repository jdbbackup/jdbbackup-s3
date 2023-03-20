package com.fathzer.jdbbackup.managers.s3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.ClientInspector;
import com.fathzer.jdbbackup.utils.ProxySettings;

class S3ManagerTest {
	
	@Test
	void testScheme() {
		assertEquals("s3", new S3Manager().getScheme());
	}
	
	@Test
	void testBuildClient() {
		S3Manager s3 = new S3Manager();
		AmazonS3Client client = (AmazonS3Client) s3.getClient(new BasicAWSCredentials("access", "secret"), "region");
		assertEquals("region",client.getRegionName());
		assertNull(client.getClientConfiguration().getProxyHost());
		// No auth => client should use a customized credential provider chain
		assertNotEquals(ClientInspector.getDefaultProviderClass(), ClientInspector.getCredentialsProviderChain(client).getClass());

		s3.setProxy(ProxySettings.fromString("user:password@host:3128"));
		client = (AmazonS3Client) s3.getClient(null, "region");
		assertEquals("host", client.getClientConfiguration().getProxyHost());
		assertEquals(3128, client.getClientConfiguration().getProxyPort());
		assertEquals("user", client.getClientConfiguration().getProxyUsername());
		assertEquals("password", client.getClientConfiguration().getProxyPassword());
		// No auth => client should use a S3CredentialProviderChain
		assertEquals(ClientInspector.getDefaultProviderClass(), ClientInspector.getCredentialsProviderChain(client).getClass());
	}

	@Test
	void testValidate() {
		S3Manager s3 = new S3Manager();
		try (MockedConstruction<DefaultAwsRegionProviderChain> mock = mockConstruction(DefaultAwsRegionProviderChain.class, (chain, context) -> {
			when(chain.getRegion()).thenReturn("default-region");
		})) {
			// Test region is specified
			BucketPath bp = s3.validate("eu-west-1:bucket/path", s->s+".txt");
			assertNull(bp.getCredentials());
			assertEquals("eu-west-1", bp.getRegion());
			assertEquals("bucket", bp.getBucket());
			assertEquals("path.txt", bp.getPath());
		
			// Test default region should be used 
			bp = s3.validate("bucket/path", s->s+".txt");
			assertEquals("default-region", bp.getRegion());
		}
		
		// Test default region unavailable
		try (MockedConstruction<DefaultAwsRegionProviderChain> mock = mockConstruction(DefaultAwsRegionProviderChain.class, (chain, context) -> {
			when(chain.getRegion()).thenThrow(SdkClientException.class);
		})) {
			assertThrows(IllegalArgumentException.class, () -> s3.validate("bucket/path", s->s+".txt"));
		}
	}
}
