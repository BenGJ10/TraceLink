package com.urlshortner.tracelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TraceLinkApp{

    public static void main(String[] args) {
        SpringApplication.run(TraceLinkApp.class, args);
    }

}
