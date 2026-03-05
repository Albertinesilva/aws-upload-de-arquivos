package com.albertsilva.dscatalog.security.jwt;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import com.albertsilva.dscatalog.security.user.CustomUserDetails;

/**
 * Customiza o JWT (JSON Web Token) de acesso adicionando claims específicas do
 * domínio da aplicação.
 * 
 * <p>
 * Esta customização inclui:
 * <ul>
 * <li>Adição de informações do usuário (ID e nome)</li>
 * <li>Inclusão das roles do usuário</li>
 * </ul>
 * Observações:
 * <ul>
 * <li>Aplica-se somente a tokens do tipo
 * {@link OAuth2TokenType#ACCESS_TOKEN}</li>
 * <li>Ignora o fluxo de {@link AuthorizationGrantType#CLIENT_CREDENTIALS}</li>
 * <li>Não realiza consultas adicionais ao banco de dados; utiliza apenas os
 * dados carregados em {@link CustomUserDetails}</li>
 * </ul>
 * </p>
 * 
 * @author Albert
 * @since 2026-03-05
 */
@Component
public class JwtCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

  private static Logger logger = LoggerFactory.getLogger(JwtCustomizer.class);

  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_FIRST_NAME = "firstName";
  private static final String CLAIM_ROLES = "roles";

  /**
   * Personaliza o token JWT de acesso.
   * 
   * <p>
   * Adiciona claims customizadas contendo:
   * <ul>
   * <li>userId - ID do usuário</li>
   * <li>firstName - primeiro nome do usuário</li>
   * <li>roles - lista de roles do usuário</li>
   * </ul>
   * </p>
   *
   * @param context Contexto do JWT sendo gerado, contém informações do principal
   *                e do tipo de token.
   * @throws IllegalArgumentException Se o principal não estiver presente ou não
   *                                  for do tipo {@link CustomUserDetails}.
   * @author Albert
   * @since 2026-03-05
   */
  @Override
  public void customize(JwtEncodingContext context) {

    if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
      logger.debug("Token type is not ACCESS_TOKEN, skipping customization.");
      return;
    }

    if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
      context.getClaims()
          .claim(CLAIM_USER_ID, context.getRegisteredClient().getClientId())
          .claim(CLAIM_FIRST_NAME, context.getRegisteredClient().getClientName())
          .claim("scopes", context.getAuthorizedScopes());

      logger.debug("Client credentials flow detected, added client info to claims.");
      logger.debug("Client ID: " + context.getRegisteredClient().getClientId());
      logger.debug("Client Name: " + context.getRegisteredClient().getClientName());
      logger.debug("Authorized Scopes: " + context.getAuthorizedScopes());
      return;
    }

    Authentication authentication = context.getPrincipal();
    if (authentication == null) {
      logger.warn("Authentication principal is null, cannot customize JWT claims.");
      logger.debug("Authentication object: " + authentication);
      return;
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserDetails user)) {
      logger.warn("Principal is not an instance of UserDetails, cannot customize JWT claims.");
      logger.debug("Principal class: " + principal.getClass().getName());
      logger.debug("Principal object: " + principal);

      return;
    }

    var roles = user.getAuthorities().stream()
        .map(auth -> auth.getAuthority())
        .collect(Collectors.toList());

    context.getClaims()
        .claim(CLAIM_USER_ID, user.getUsername())
        .claim(CLAIM_FIRST_NAME, user.getUsername())
        .claim(CLAIM_ROLES, roles);

    logger.debug("Customized JWT claims added for user: " + user.getUsername());
    logger.debug("User ID claim: " + user.getUsername());
    logger.debug("Roles claim: " + roles);
  }

}