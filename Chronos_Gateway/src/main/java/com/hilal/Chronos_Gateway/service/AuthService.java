package com.hilal.Chronos_Gateway.service;

import com.hilal.Chronos_Gateway.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .authorities(u.getRoles().split(","))
                        .build())
                .switchIfEmpty(reactor.core.publisher.Mono.error(new UsernameNotFoundException("User not found")));
    }
}