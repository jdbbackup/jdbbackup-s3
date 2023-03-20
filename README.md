# jdbbackup-s3
An Amazon S3 JDBBackup destination manager

## Destination format
\[accessKey:secretKey@\]\[region:\]bucket/path

If *accessKey:secretKey* are not provided, the manager uses the [default AWS credentials provider chain](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html).

If *region* is not provided, the manager uses the [default AWS region provider chain](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/regions/providers/DefaultAwsRegionProviderChain.html).

## TODO
Remove the toBeDeleted folder and task in .github/workflows/build.yml