package com.minwonhaeso.esc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableScheduling
@EnableTransactionManagement
@EnableJpaAuditing
@EnableCaching
@EnableWebMvc
@SpringBootApplication
public class EscServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EscServerApplication.class, args);
    }
}
