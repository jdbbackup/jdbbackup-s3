package com.fathzer.jdbbackup.managers.s3;

import java.net.PasswordAuthentication;
import java.util.function.Function;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.internal.BucketNameUtils;
import com.fathzer.jdbbackup.DefaultPathDecoder;
import com.fathzer.jdbbackup.DestinationManager;
import com.fathzer.jdbbackup.utils.LoginParser;

class BucketPath {
	private static final String WRONG_DEST_MESS = "Destination should conform to the format [key:secret@]bucket/path";
	private BasicAWSCredentials credentials;
	private String region;
	private String bucket;
	private String path;
	
	BucketPath(String fullPath, Function<String,CharSequence> extensionBuilder) {
		int index = fullPath.indexOf('@');
		if (index >= 0) {
			final String authString = DefaultPathDecoder.INSTANCE.decodePath(fullPath.substring(0, index));
			final PasswordAuthentication login = LoginParser.fromString(authString);
			this.credentials = new BasicAWSCredentials(login.getUserName(), String.valueOf(login.getPassword()));
			fullPath = fullPath.substring(index+1);
		}
		parsePath(fullPath, extensionBuilder);
	}

	private void parsePath(String path, Function<String, CharSequence> extensionBuilder) {
		int index = path.indexOf(DestinationManager.URI_PATH_SEPARATOR);
		if (index<=0) {
			throw new IllegalArgumentException("Unable to find bucket name. "+WRONG_DEST_MESS);
		}
		parseBucket(path.substring(0, index));
		this.path = path.substring(index+1);
		if (this.path.isEmpty()) {
			throw new IllegalArgumentException("Unable to locate destination path. "+WRONG_DEST_MESS);
		}
		this.path = DefaultPathDecoder.INSTANCE.decodePath(this.path, extensionBuilder);
	}
	
	private void parseBucket(String bucketAddress) {
		int index = bucketAddress.indexOf(':');
		if (index<=0) {
			this.bucket = bucketAddress;
		} else {
			this.bucket = bucketAddress.substring(index+1);
			this.region = bucketAddress.substring(0, index);
		}
		BucketNameUtils.validateBucketName(this.bucket);
	}
	
	String getBucket() {
		return bucket;
	}
	String getPath() {
		return path;
	}
	BasicAWSCredentials getCredentials() {
		return credentials;
	}
	String getRegion() {
		return region;
	}
	void setRegion(String region) {
		this.region = region;
	}
}
