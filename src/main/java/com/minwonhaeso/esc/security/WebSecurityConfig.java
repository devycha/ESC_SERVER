package com.minwonhaeso.esc.security;

import com.minwonhaeso.esc.member.service.CustomerMemberDetailsService;
import com.minwonhaeso.esc.security.auth.jwt.JwtAuthenticationFilter;
import com.minwonhaeso.esc.security.auth.jwt.JwtEntryPoint;
import com.minwonhaeso.esc.security.oauth2.handler.OAuth2FailureHandler;
import com.minwonhaeso.esc.security.oauth2.handler.OAuth2SuccessHandler;
import com.minwonhaeso.esc.security.oauth2.service.CustomerOAuth2MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtEntryPoint jwtEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomerMemberDetailsService customerMemberDetailsService;
    private final CustomerOAuth2MemberService oAuth2UserService;

    private final OAuth2SuccessHandler successHandler;
    private final OAuth2FailureHandler failureHandler;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/h2-console/**", "/favicon.ico");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                    .logout().disable()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .csrf().disable()
                    .authorizeRequests()
                    .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                    .antMatchers("/stadium/**", "/stadiums/**", "/members/signUp","/members/email-dup","/email-auth","/email-authentication").permitAll()
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(jwtEntryPoint)
                .and()
                    .oauth2Login()
//                    .defaultSuccessUrl("/")
                    .successHandler(successHandler)
                    .failureHandler(failureHandler)
                    .authorizationEndpoint()
                    .baseUri("/oauth2/authorization")
                .and()
//                    .redirectionEndpoint()
//                    .baseUri("*/oauth2/code/*")
//                .and()
                    .userInfoEndpoint()
                    .userService(oAuth2UserService);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customerMemberDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}
