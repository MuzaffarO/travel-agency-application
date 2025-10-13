package com.travelbackendapp.travelmanagement.di;

import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class AwsModule {

    @Provides
    @Singleton
    DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder().build();
    }

    @Provides
    @Singleton
    DynamoDbEnhancedClient enhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    @Provides
    @Singleton
    CognitoIdentityProviderClient cognitoClient() {
        return CognitoIdentityProviderClient.builder().build();
    }

    @Provides
    @Singleton
    SqsClient sqsClient() {
        return SqsClient.builder().build();
    }

    @Provides
    @Named("userPoolId")
    String userPoolId() {
        String v = System.getenv("COGNITO_USER_POOL_ID");
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Missing env var 'COGNITO_USER_POOL_ID'");
        }
        return v;
    }

    @Provides @Singleton
    S3Client s3(@Named("AWS_REGION") String awsRegion) {
        return S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(awsRegion))
                .build();
    }

    @Provides @Named("AVATARS_BUCKET")
    static String avatarsBucket() {
        String b = System.getenv("AVATARS_BUCKET");
        if (b == null || b.isBlank()) throw new IllegalStateException("AVATARS_BUCKET is not set");
        return b.trim();
    }


    @Provides @Singleton
    S3Presigner s3Presigner(@Named("AWS_REGION") String awsRegion) {
        return S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(awsRegion))
                .credentialsProvider(software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create())
                .build();
    }

}
