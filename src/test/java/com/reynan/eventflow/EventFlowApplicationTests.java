package com.reynan.eventflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EventFlowApplicationTests {

    @Test
    void contextLoads() {
    }

}
