package com.reynan.eventflow;

import org.springframework.boot.SpringApplication;

public class TestEventFlowApplication {

    public static void main(String[] args) {
        SpringApplication.from(EventFlowApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
