package com.trainingplatform.enrollments.core.course;

import com.trainingplatform.catalog.api.CoursePublishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class CoursePublishedHandler {

    private static final Logger log = LoggerFactory.getLogger(CoursePublishedHandler.class);

    @EventListener
    void on(CoursePublishedEvent event) {
        log.info("Course {} is open for enrollment", event.courseId());
    }
}
