package com.devops.app.config;

import com.devops.app.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.stream.Collectors;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
            .map(appUser -> User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .authorities(appUser.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()))
                .disabled(!appUser.isEnabled())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found: " + username));
    }
}
