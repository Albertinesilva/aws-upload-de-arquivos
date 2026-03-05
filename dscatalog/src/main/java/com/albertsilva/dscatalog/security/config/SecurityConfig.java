package com.albertsilva.dscatalog.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuração de segurança do Resource Server.
 *
 * <p>
 * Esta classe define:
 * <ul>
 * <li>Regras de autorização para diferentes perfis de usuário (roles)</li>
 * <li>Configuração de CORS para chamadas de qualquer origem</li>
 * <li>Desabilita CSRF para simplificar chamadas via API (REST)</li>
 * <li>Habilita OAuth2 JWT como método de autenticação principal</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Observações:
 * <ul>
 * <li>O matcher desta configuração é global ("/**") e não conflita com os
 * endpoints do Authorization Server</li>
 * <li>Utiliza BCrypt para criptografia de senhas</li>
 * </ul>
 * </p>
 * 
 * Autor: Albert
 * Data: 2026-03-05
 */
@Configuration
public class SecurityConfig {

    /** Endpoints públicos, sem autenticação */
    private static final String[] PUBLIC = { "/h2-console/**" };

    /** Endpoints acessíveis para OPERATOR e ADMIN */
    private static final String[] OPERATOR_OR_ADMIN = { "/products/**", "/categories/**" };

    /** Endpoints restritos apenas a ADMIN */
    private static final String[] ADMIN = { "/users/**" };

    /**
     * Configura o {@link SecurityFilterChain} do Resource Server.
     *
     * <p>
     * Esta configuração define:
     * <ul>
     * <li>Autorização baseada em roles</li>
     * <li>Permissão de GET público para endpoints OPERATOR e ADMIN</li>
     * <li>Proteção de outros endpoints com autenticação obrigatória</li>
     * <li>Habilitação de OAuth2 JWT para autenticação</li>
     * <li>Desabilita CSRF e frameOptions (necessário para H2 console)</li>
     * </ul>
     * </p>
     *
     * @param http instância de {@link HttpSecurity}
     * @return {@link SecurityFilterChain} configurado
     * @throws Exception caso ocorra erro na configuração
     */
    @Bean
    @Order(2)
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**") // aplica à API, não conflita com Auth Server
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC).permitAll()
                        .requestMatchers(HttpMethod.GET, OPERATOR_OR_ADMIN).permitAll()
                        .requestMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR", "ADMIN")
                        .requestMatchers(ADMIN).hasRole("ADMIN")
                        .anyRequest().authenticated())
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Configura CORS (Cross-Origin Resource Sharing).
     *
     * <p>
     * Permite chamadas de qualquer origem, com métodos GET, POST, PUT, DELETE e
     * PATCH.
     * Headers permitidos: Authorization, Content-Type.
     * </p>
     *
     * @return {@link CorsConfigurationSource} configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    /**
     * Configura o {@link PasswordEncoder} utilizado pelo Spring Security.
     *
     * <p>
     * Utiliza {@link BCryptPasswordEncoder} para criptografar senhas antes de
     * persistir.
     * </p>
     *
     * @return {@link PasswordEncoder} configurado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}