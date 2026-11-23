package centralize.individual_research.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Public terminology-store endpoints accessible without any authentication
                        .requestMatchers("/public/**").permitAll()
                        // Any other future endpoint requires authentication
                        .anyRequest().authenticated()
                );

        /*
         * =========================================================================
         * Future JWT Security Extension Point
         * =========================================================================
         * To introduce JWT authentication for future business operations:
         * 1. Add `spring-boot-starter-oauth2-resource-server` dependency, OR
         * 2. Configure a custom `JwtAuthenticationFilter` extending `OncePerRequestFilter`:
         *    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
         * 3. Or configure OAuth2 Resource Server DSL:
         *    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
         * 4. Keep `/public/**` permitted and apply `.authenticated()` / `.hasRole()` to new endpoints:
         *    .requestMatchers("/public/**").permitAll()
         *    .requestMatchers("/api/admin/**").hasRole("ADMIN")
         *    .requestMatchers("/api/**").authenticated()
         * =========================================================================
         */

        return http.build();
    }
}
