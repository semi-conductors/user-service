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
        rabbitTemplate.convertAndSend(exchange, "user.registered", event);
    }

    @Override
    public void publishPasswordResetRequestedEvent(PasswordResetRequestedEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());
        rabbitTemplate.convertAndSend(exchange, "user.password.reset.requested", event);
    }

    @Override
    public void publishProfileDisabledEvent(ProfileDisabledEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());
        rabbitTemplate.convertAndSend(exchange, "user.profile.disabled", event);
    }

    @Override
    public void publishIdentityVerificationApprovedEvent(IdentityVerificationApprovedEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());
        rabbitTemplate.convertAndSend(exchange, "user.identity.verification.approved", event);
    }

    @Override
    public void publishIdentityVerificationRejectedEvent(IdentityVerificationRejectedEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());
        rabbitTemplate.convertAndSend(exchange, "user.identity.verification.rejected", event);
    }

    @Override
    public void publishReportCreatedEvent(ReportCreatedEvent event) {
        log.info("Publishing event: " + event.getClass().getSimpleName() + " " + event.toString());
        rabbitTemplate.convertAndSend(exchange, "report.submitted", event);
    }
}