package com.albertsilva.dscatalog.security.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração do Authorization Server para o projeto.
 *
 * <p>
 * Esta classe define:
 * <ul>
 * <li>Endpoints OAuth2 (token, authorize, introspect, etc.)</li>
 * <li>Clientes registrados com diferentes fluxos de autorização</li>
 * <li>Configurações de tempo de vida e políticas de tokens JWT</li>
 * </ul>
 * </p>
 *
 * <p>
 * Permite que o Spring Authorization Server gerencie tokens JWT, incluindo:
 * <ul>
 * <li>Client Credentials</li>
 * <li>Authorization Code</li>
 * <li>Refresh Token</li>
 * </ul>
 * </p>
 * 
 * Autor: Albert
 * Data: 2026-03-05
 */
@Configuration
public class AuthorizationServerConfig {

  @Value("${security.oauth2.client.client-id}")
  private String clientId;

  @Value("${security.oauth2.client.client-secret}")
  private String clientSecret;

  @Value("${jwt.days-to-expire}")
  private Integer jwtDaysToExpire;

  @Value("${jwt.duration}")
  private Integer jwtDuration;

  /**
   * Configura a SecurityFilterChain específica para o Authorization Server.
   *
   * <p>
   * Esta configuração:
   * <ul>
   * <li>Aplica apenas aos endpoints do Authorization Server (matcher
   * exclusivo)</li>
   * <li>Desabilita CSRF nos endpoints OAuth2</li>
   * <li>Exige autenticação para qualquer requisição</li>
   * <li>Aplica o {@link OAuth2AuthorizationServerConfigurer} que configura
   * endpoints padrão do OAuth2</li>
   * <li>Opcionalmente habilita validação de JWT no próprio Authorization
   * Server</li>
   * </ul>
   * </p>
   *
   * @param http instância de {@link HttpSecurity}
   * @return {@link SecurityFilterChain} configurado para o Authorization Server
   * @throws Exception caso ocorra erro na configuração de segurança
   */
  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerFilterChain(HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

    // Aplica configuração apenas aos endpoints do Authorization Server
    http
        .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .apply(authorizationServerConfigurer);

    // Habilita validação JWT no Authorization Server (opcional)
    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }

  /**
   * Registra clientes OAuth2 em memória.
   *
   * <p>
   * O cliente configurado neste método:
   * <ul>
   * <li>Suporta Client Credentials, Authorization Code e Refresh Token</li>
   * <li>Possui escopos "read" e "write"</li>
   * <li>Define tempo de vida de access token e refresh token</li>
   * <li>Não permite reuso de refresh token (mais seguro)</li>
   * <li>Define redirect URI necessária para Authorization Code</li>
   * </ul>
   * </p>
   *
   * @param encoder {@link PasswordEncoder} para criptografar o client secret
   * @return {@link RegisteredClientRepository} contendo o cliente registrado
   */
  @Bean
  public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {

    RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId(clientId)
        .clientSecret(encoder.encode(clientSecret))
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope("read")
        .scope("write")
        .tokenSettings(TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofMinutes(jwtDuration))
            .refreshTokenTimeToLive(Duration.ofDays(jwtDaysToExpire))
            .reuseRefreshTokens(false)
            .build())
        .build();

    return new InMemoryRegisteredClientRepository(client);
  }

  /**
   * Configurações gerais do Authorization Server.
   *
   * <p>
   * Define o issuer (emissor) dos tokens e permite customizações futuras
   * de endpoints e políticas do Authorization Server.
   * </p>
   *
   * @return {@link AuthorizationServerSettings} configurado
   */
  @Bean
  public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
        .issuer("http://localhost:8080")
        .build();
  }
}