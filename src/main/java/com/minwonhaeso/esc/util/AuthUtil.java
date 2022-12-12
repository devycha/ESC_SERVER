package com.minwonhaeso.esc.util;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtil {

    public static String generateEmailAuthNum() {
        java.util.Random generator = new java.util.Random();
        generator.setSeed(System.currentTimeMillis());
        String result =  String.valueOf(generator.nextInt(1000000) % 1000000);
        if(result.length() !=6){
            for (int i = 0; i < 6 - result.length(); i++) {
                result = "0" + result;
            }
        }
        return result;
    }

}
