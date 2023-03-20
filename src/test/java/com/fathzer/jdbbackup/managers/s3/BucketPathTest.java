package com.fathzer.jdbbackup.managers.s3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.jdbbackup.utils.BasicExtensionBuilder;

class BucketPathTest {

	@Test
	void test() {
		final BasicExtensionBuilder eb = BasicExtensionBuilder.INSTANCE;
		BucketPath bucketPath = new BucketPath("a:b@eu-west-3:bucket/jdb/test", eb);
		assertEquals("a",bucketPath.getCredentials().getAWSAccessKeyId());
		assertEquals("b",bucketPath.getCredentials().getAWSSecretKey());
		assertEquals("bucket",bucketPath.getBucket());
		assertEquals("eu-west-3",bucketPath.getRegion());
		assertEquals("jdb/test.sql.gz",bucketPath.getPath());
		
		bucketPath = new BucketPath("bucket/jdb/test", eb);
		assertNull(bucketPath.getCredentials());
		assertEquals("bucket",bucketPath.getBucket());
		assertNull(bucketPath.getRegion());
		assertEquals("jdb/test.sql.gz",bucketPath.getPath());
		
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("", eb));
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("a", eb));
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("a:b", eb));
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("a:b@", eb));
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("a:b@bucket", eb));
		// Bucket names that don't comply with S3 requirements
		assertThrows(IllegalArgumentException.class, () -> new BucketPath(":region/file", eb)); // empty
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("a/file", eb)); //too small
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("CantHaveUpperAtFirst/file", eb));
		assertThrows(IllegalArgumentException.class, () -> new BucketPath("withInvalidCharInIt/file", eb));
	}

}
