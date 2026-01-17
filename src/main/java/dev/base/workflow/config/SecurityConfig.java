package dev.base.workflow.config;

import dev.base.workflow.security.JwtAuthenticationFilter;
import dev.base.workflow.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration for OAuth2 login with Google and GitHub.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
        private final JwtAuthenticationFilter jwtAuthFilter;
        private final AppConfig appConfig;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                configureHttpSecurity(http);
                return http.build();
        }

        private void configureHttpSecurity(HttpSecurity http) throws Exception {
                http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(this::configureAuthorizationRules)
                                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2SuccessHandler))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        private void configureAuthorizationRules(
                        org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
                var security = appConfig.getSecurity();
                auth.requestMatchers(security.getPublicPaths().toArray(String[]::new)).permitAll()
                                .requestMatchers(security.getAuthPaths().toArray(String[]::new)).permitAll()
                                .requestMatchers(security.getActuatorPaths().toArray(String[]::new)).permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = createCorsConfiguration();
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        private CorsConfiguration createCorsConfiguration() {
                var cors = appConfig.getCors();
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(cors.getAllowedOrigins());
                config.setAllowedMethods(cors.getAllowedMethods());
                config.setAllowedHeaders(cors.getAllowedHeaders());
                config.setAllowCredentials(true);
                config.setExposedHeaders(cors.getExposedHeaders());
                return config;
        }
}
