# jdbbackup-s3
An Amazon S3 JDBBackup destination manager

## Authentication
The manager uses the [default AWS credentials provider chain](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html). In addition, you can set the **AWS_APPLICATION_CREDENTIALS** environment variable can be used to set the path of a file that contains the application Amazon credentials in json format:  
```
{"accessKey":"myKey","secretKey":"jhkj7832dsd"}
```
