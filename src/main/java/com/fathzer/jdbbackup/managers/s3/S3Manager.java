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

/** A Destination manager that saves to Amazon S3 bucket.
 * <br>The address format is: s3://[accessKey:secretKey@][region:]bucket/path.
 * <br>If <i>accessKey:secretKey</i> are omitted, the manager uses the <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html">default AWS credentials provider chain</a>
 * <br>If *region* is not provided, the manager uses the <a href="https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/regions/providers/DefaultAwsRegionProviderChain.html">default AWS region provider chain</a>. 
 */
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

	/** Builds an S3 client.
	 * @param credentials The client credentials or null if they are not defined 
	 * @param region The region (can't be null)
	 * @return a new AmazonS3 client
	 */
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
				if (proxy.getLogin().getPassword().length>0) {
					conf.withProxyPassword(String.valueOf(proxy.getLogin().getPassword()));
				}
			}
			clientBuilder.withClientConfiguration(conf);
		}
		return clientBuilder.build();
	}
}
