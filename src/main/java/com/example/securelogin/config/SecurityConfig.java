package com.example.securelogin.config;

import com.example.securelogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration    // This class provides Spring beans (objects Spring manages)
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean  // Spring will call this method and manage the returned object
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // Integrates the CorsConfig MVC CORS registry definitions
                // CSRF is disabled because this API uses JWT stateless authentication instead of server-side sessions.
                // Since JWTs are stored in client-side storage (e.g. localStorage) and sent via the Authorization header,
                // the browser does not automatically attach them to cross-site requests, mitigating cookie-based CSRF attacks.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self' 'unsafe-inline'"))
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/dashboard.html", "/favicon.ico", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // Role-based access control
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // All other API endpoints must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
