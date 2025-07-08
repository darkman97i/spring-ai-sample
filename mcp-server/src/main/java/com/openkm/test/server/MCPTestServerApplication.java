package com.openkm.test.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.openkm.test")
public class MCPTestServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MCPTestServerApplication.class, args);
    }
}
