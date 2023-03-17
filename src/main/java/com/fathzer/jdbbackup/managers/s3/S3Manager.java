package com.fathzer.jdbbackup.managers.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fathzer.jdbbackup.DestinationManager;
import com.fathzer.jdbbackup.utils.ProxySettings;

public class S3Manager implements DestinationManager<BucketPath> {
	private ProxySettings proxy;
	private AmazonS3 s3;
	
	@Override
	public void setProxy(ProxySettings proxy) {
		this.proxy = proxy;
		this.s3 = null;
	}

	@Override
	public String getScheme() {
		return "s3";
	}

	@Override
	public BucketPath validate(String path, Function<String, CharSequence> extensionBuilder) {
		return new BucketPath(path, extensionBuilder);
	}

	@Override
	public void send(InputStream in, long size, BucketPath destination) throws IOException {
		getClient().putObject(destination.getBucket(), destination.getPath(), in, new ObjectMetadata());
	}
	
	private AmazonS3 getClient() {
		if (s3==null) {
			s3 = buildClient();
		}
		return s3;
	}

	protected AmazonS3 buildClient() {
		final AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withCredentials(new AWSCredentialsProviderChain(Arrays.asList(JSonAWSCredentials.INSTANCE, new DefaultAWSCredentialsProviderChain())));
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
