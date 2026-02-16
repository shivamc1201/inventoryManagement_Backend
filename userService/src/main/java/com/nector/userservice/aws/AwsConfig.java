package com.nector.userservice.aws;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    private final String awsRegion = "us-east-1";
    private final String awsAccessKey;
    private final String awsSecretKey;

    public AwsConfig(@Qualifier("awsAccessKey") String awsAccessKey,
                     @Qualifier("awsSecretKey") String awsSecretKey) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
    }
}
