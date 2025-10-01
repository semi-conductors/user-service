package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.domain.dto.event.ProfileDisabledEvent;
import com.rentmate.service.user.config.RabbitMQConfig;
import com.rentmate.service.user.domain.dto.event.PasswordResetRequestedEvent;
import com.rentmate.service.user.domain.dto.event.UserRegisteredEvent;
import com.rentmate.service.user.service.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor @Slf4j
public class UserEventPublisherImpl implements UserEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange = RabbitMQConfig.EXCHANGE;

    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());

        //TODO: publish event to rabbitmq exchange, key = user.registered
    }

    @Override
    public void publishPasswordResetRequestedEvent(PasswordResetRequestedEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());

        //TODO: publish event to rabbitmq exchange, key = user.password.reset.requested
    }

    @Override
    public void publishProfileDisabledEvent(ProfileDisabledEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());

        //TODO: publish event to rabbitmq exchange, key = user.profile.disabled
    }
}