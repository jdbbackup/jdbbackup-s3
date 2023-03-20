package com.fathzer.jdbbackup.managers.s3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;

class S3ManagerTest {

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
