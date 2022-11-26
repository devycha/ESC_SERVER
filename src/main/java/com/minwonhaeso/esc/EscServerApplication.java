package com.minwonhaeso.esc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class EscServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EscServerApplication.class, args);
    }

}
