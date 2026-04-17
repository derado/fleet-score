package com.fleetscore.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
    Jwt jwt,
    Refresh refresh,
    int resetTtlMin
) {

  public record Jwt(
      String issuer,
      int accessTtlMin,
      int refreshTtlDays
  ) {}

  public record Refresh(
      String cookieName,
      boolean cookieSecure,
      String cookieSameSite,
      String cookieDomain
  ) {}
}
