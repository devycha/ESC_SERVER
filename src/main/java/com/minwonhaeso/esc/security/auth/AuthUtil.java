package com.minwonhaeso.esc.security.auth;


import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    public String generateAuthNo() {
        java.util.Random generator = new java.util.Random();
        generator.setSeed(System.currentTimeMillis());
        return String.valueOf(generator.nextInt(1000000) % 1000000);
    }
}
