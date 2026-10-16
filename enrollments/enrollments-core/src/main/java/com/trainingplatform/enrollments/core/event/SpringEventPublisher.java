package com.trainingplatform.enrollments.core.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("enrollmentsSpringEventPublisher")
class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher delegate;

    SpringEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publish(Object event) {
        delegate.publishEvent(event);
    }
}
