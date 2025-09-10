package com.event.babyshower.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.event")
@Getter @Setter
public class EventProperties {
    private String title;
    private String startIso;
    private String endIso;
    private String venue;
    private String address;
}

