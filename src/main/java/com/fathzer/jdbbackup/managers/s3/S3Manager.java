package com.fathzer.jdbbackup.managers.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.function.Function;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fathzer.jdbbackup.DestinationManager;
import com.fathzer.jdbbackup.utils.ProxySettings;

public class S3Manager implements DestinationManager<BucketPath> {
	private ProxySettings proxy;
	
	@Override
	public void setProxy(ProxySettings proxy) {
		this.proxy = proxy;
	}

	@Override
	public String getScheme() {
		return "s3";
	}

	@Override
	public BucketPath validate(String path, Function<String, CharSequence> extensionBuilder) {
		final BucketPath result = new BucketPath(path, extensionBuilder);
		if (result.getRegion()==null) {
			try {
				result.setRegion(new DefaultAwsRegionProviderChain().getRegion());
			} catch (SdkClientException e) {
				throw new IllegalArgumentException("AWS region is not specified and default provider can't find it", e);
			}
		}
		return result;
	}

	@Override
	public void send(InputStream in, long size, BucketPath destination) throws IOException {
		final AmazonS3 client = getClient(destination.getCredentials(), destination.getRegion());
		final ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(size);
		try {
			client.putObject(destination.getBucket(), destination.getPath(), in, metadata);
		} catch (SdkClientException e) {
			throw new IOException(e);
		}
	}

	protected AmazonS3 getClient(AWSCredentials credentials, String region) {
		final AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(region);
		if (credentials!=null) {
			clientBuilder.withCredentials(new AWSCredentialsProviderChain(Collections.singletonList(new AWSStaticCredentialsProvider(credentials))));
		}
		if (proxy!=null) {
			final ClientConfiguration conf = new ClientConfiguration();
			conf.withProxyHost(proxy.getHost()).withProxyPort(proxy.getPort());
			if (proxy.getLogin()!=null) {
				conf.withProxyUsername(proxy.getLogin().getUserName());
				if (proxy.getLogin()!=null && proxy.getLogin().getPassword().length>0) {
					conf.withProxyPassword(String.valueOf(proxy.getLogin().getPassword()));
				}
			}
			clientBuilder.withClientConfiguration(conf);
		}
		return clientBuilder.build();
	}
}
