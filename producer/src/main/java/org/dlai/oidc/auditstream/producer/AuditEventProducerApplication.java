package org.dlai.oidc.auditstream.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuditEventProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditEventProducerApplication.class, args);
    }
}
