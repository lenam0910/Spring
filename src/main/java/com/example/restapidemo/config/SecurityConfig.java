package com.example.restapidemo.config;

import java.security.KeyStore.SecretKeyEntry;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.restapidemo.entity.User;
import com.example.restapidemo.repository.UserRepository;
import com.example.restapidemo.security.CustomOAuth2UserService;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(UserRepository userRepository) {
                return username -> {
                        User user = userRepository.findByEmail(username)
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "User not found: " + username));
                        return org.springframework.security.core.userdetails.User
                                        .withUsername(user.getEmail())
                                        .password(user.getPassword())
                                        .roles(user.getRole())
                                        .build();
                };
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/signup", "/login", "/css/**").permitAll()
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/users/**").hasRole("USER")
                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/users", true)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/users", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutRequestMatcher(
                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                "/logout", "GET")) // Chấp nhận GET
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());
                // http.oauth2ResourceServer(
                // oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())));
                // // Enable
                // // JWT
                // // authentication
                return http.build();
        }

        // @Value("${jwt.secret}")
        // private String jwtSecret;

        // @Bean
        // JwtDecoder jwtDecoder() {
        // SecretKeySpec secretKeySpec = new
        // SecretKeySpec("your-secret-key".getBytes());
        // NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
        // }
}