package com.nector.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptedCredentialsConfig {
    
    @Value("${aws.access-key-id.encrypted}")
    private String encryptedAwsAccessKey;
    
    @Value("${aws.secret-access-key.encrypted}")
    private String encryptedAwsSecretKey;
    
    @Value("${twilio.account-sid.encrypted}")
    private String encryptedTwilioSid;
    
    @Value("${twilio.auth-token.encrypted}")
    private String encryptedTwilioToken;
    
    @Value("${jwt.secret.encrypted}")
    private String encryptedJwtSecret;
    
    @Bean
    public String awsAccessKey() {
        return CredentialEncryption.decrypt(encryptedAwsAccessKey);
    }
    
    @Bean
    public String awsSecretKey() {
        return CredentialEncryption.decrypt(encryptedAwsSecretKey);
    }
    
    @Bean
    public String twilioAccountSid() {
        return CredentialEncryption.decrypt(encryptedTwilioSid);
    }
    
    @Bean
    public String twilioAuthToken() {
        return CredentialEncryption.decrypt(encryptedTwilioToken);
    }
    
    @Bean
    public String jwtSecret() {
        return CredentialEncryption.decrypt(encryptedJwtSecret);
    }
}