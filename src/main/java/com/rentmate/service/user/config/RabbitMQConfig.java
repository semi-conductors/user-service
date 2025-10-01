package com.rentmate.service.user.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "users.exchange";

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE, true, false);
    }
}
