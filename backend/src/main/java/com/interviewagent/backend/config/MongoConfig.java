package com.interviewagent.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Value("${SPRING_DATA_MONGODB_URI:mongodb://mongodb-service:27017/interview-agent}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        System.out.println("============== MANUALLY CREATING MONGO CLIENT WITH URI: " + mongoUri + " ==============");
        return MongoClients.create(mongoUri);
    }
}
