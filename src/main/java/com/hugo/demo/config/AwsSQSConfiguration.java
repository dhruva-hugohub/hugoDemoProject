package com.hugo.demo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AwsSQSConfiguration {

    private SqsAsyncClient sqsAsyncClient;

    @Bean
    public SqsAsyncClient initializeSqsClient() {
        this.sqsAsyncClient =
            SqsAsyncClient.builder().region(Region.AP_SOUTH_1).credentialsProvider(DefaultCredentialsProvider.builder().build()).build();
        return this.sqsAsyncClient;
    }

    public SqsAsyncClient getSqsAsyncClient() {
        if (sqsAsyncClient == null) {
            throw new IllegalStateException("SQS Client not initialized. Please login first.");
        }
        return sqsAsyncClient;
    }
}