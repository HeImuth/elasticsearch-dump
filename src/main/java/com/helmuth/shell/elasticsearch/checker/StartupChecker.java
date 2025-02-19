package com.helmuth.shell.elasticsearch.checker;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupChecker implements ApplicationListener<ApplicationStartedEvent> {
    private final ElasticsearchHealthChecker healthChecker;

    public StartupChecker(ElasticsearchHealthChecker healthChecker) {
        this.healthChecker = healthChecker;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        healthChecker.checkConnectivity();
    }
}
