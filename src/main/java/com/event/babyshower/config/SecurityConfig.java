package com.event.babyshower.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/rsvp", "/thanks", "/ics", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()); // htmx + simple demo; enable and add token for prod
        return http.build();
    }

   /* @Bean
    public UserDetailsService users(
            @Value("${app.admin.user}") String user,
            @Value("${app.admin.password}") String password // supports {noop}plaintext
    ){
        return new InMemoryUserDetailsManager(
                User.withUsername(user).password(password).roles("ADMIN").build()
        );
    }*/
}

