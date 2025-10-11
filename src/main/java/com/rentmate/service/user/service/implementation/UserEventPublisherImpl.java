package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.domain.dto.event.*;
import com.rentmate.service.user.config.RabbitMQConfig;
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

    @Override
    public void publishIdentityVerificationApprovedEvent(IdentityVerificationApprovedEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());

        //TODO: publish event to rabbitmq exchange, key = user.identity.verification.approved

    }

    @Override
    public void publishIdentityVerificationRejectedEvent(IdentityVerificationRejectedEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());

        //TODO: publish event to rabbitmq exchange, key = user.identity.verification.rejected
    }

    @Override
    public void publishReportCreatedEvent(ReportCreatedEvent report) {
        // TODO: Publish event to RabbitMQ/Kafka
        log.info("TODO: Publish ReportSubmitted event for report ID: {}", report.getReportId());
    }
}