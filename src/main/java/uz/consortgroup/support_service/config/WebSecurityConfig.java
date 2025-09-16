package uz.consortgroup.support_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.security.CustomAccessDeniedHandler;
import uz.consortgroup.support_service.util.AuthEntryPointJwt;
import uz.consortgroup.support_service.util.AuthTokenFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/internal/**").permitAll()

                               // support: пресеты и создание тикета — все роли, кроме SUPER_ADMIN
                                .requestMatchers(HttpMethod.GET, "/api/v1/support/presets").hasAnyAuthority(
                                        UserRole.ADMIN.name(),
                                        UserRole.MENTOR.name(),
                                        UserRole.HR.name(),
                                        UserRole.STUDENT.name()
                                )

                                .requestMatchers(HttpMethod.POST, "/api/v1/support/tickets").hasAnyAuthority(
                                        UserRole.ADMIN.name(),
                                        UserRole.MENTOR.name(),
                                        UserRole.HR.name(),
                                        UserRole.STUDENT.name()
                                )

                        // support admin: список + обновление — только SUPER_ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/v1/support/tickets").hasAnyAuthority(UserRole.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/support/presets/super-admin/**").hasAnyAuthority(UserRole.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/support/presets/super-admin/**").hasAnyAuthority(UserRole.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/support/presets/super-admin/**").hasAnyAuthority(UserRole.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/support/presets/super-admin/**").hasAnyAuthority(UserRole.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/support/tickets/**").hasAnyAuthority(UserRole.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/support/tickets/**").hasAnyAuthority(UserRole.SUPER_ADMIN.name())

                        // остальное — аутентифицированные
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
