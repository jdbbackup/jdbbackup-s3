package com.fathzer.jdbbackup.managers.s3;

import java.util.function.Function;

import com.fathzer.jdbbackup.DefaultPathDecoder;
import com.fathzer.jdbbackup.DestinationManager;

class BucketPath {
	private String bucket;
	private String path;
	
	BucketPath(String fullPath, Function<String,CharSequence> extensionBuilder) {
		int index = fullPath.indexOf(DestinationManager.URI_PATH_SEPARATOR);
		if (index<=0) {
			throw new IllegalArgumentException("Unable to bucket name. "+"FileName should conform to the format bucket/path");
		}
		this.bucket = fullPath.substring(0, index);
		this.path = fullPath.substring(index+1);
		if (this.path.isEmpty()) {
			throw new IllegalArgumentException("Unable to locate destination path. Path should conform to the format bucket/path");
		}
		this.path = DefaultPathDecoder.INSTANCE.decodePath(this.path, extensionBuilder);
	}
	
	String getBucket() {
		return bucket;
	}
	String getPath() {
		return path;
	}
}
