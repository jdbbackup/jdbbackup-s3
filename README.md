# jdbbackup-s3
An Amazon S3 [JDBBackup](https://github.com/jdbbackup/jdbbackup-core) destination manager

## Destination format
s3://\[accessKey:secretKey@\]\[region:\]bucket/path

If *accessKey:secretKey* are not provided, the manager uses the [default AWS credentials provider chain](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html).

If *region* is not provided, the manager uses the [default AWS region provider chain](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/regions/providers/DefaultAwsRegionProviderChain.html).

All the patterns detailed [here](https://github.com/jdbbackup/jdbbackup-core) are supported.

## Loggging
As other JDBBackup components, this library uses the [slf4J](https://www.slf4j.org/) framework.  
As the Java Amazon S3 library uses the [Apache Commons Logging](https://commons.apache.org/proper/commons-logging/guide.html), this library uses the [jcl-over-slf4j](https://www.slf4j.org/legacy.html#jclOverSLF4J). If you plan to bind SLF4J to JCL, you should remove the jcl-over-slf4j dependency and add the commons-logging.

Here is an example using Maven:
```
<dependency>
	<groupId>com.fathzer</groupId>
	<artifactId>jdbbackup-s3</artifactId>
	<version>...</version>
	<exclusions>
		<exclusion>
			<groupId>org.slf4j</groupId>
			<artifactId>jcl-over-slf4j</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>commons-logging</groupId>
	<artifactId>commons-logging</artifactId>
	<version>...</version>
</dependency>
```