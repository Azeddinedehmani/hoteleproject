package com.hotel.infrastructure.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER — JWT configuration properties.
 * Bound from application.properties → jwt.*
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expiration = 86400000L;
    private long refreshExpiration = 604800000L;

    public String getSecret()                        { return secret; }
    public void setSecret(String secret)             { this.secret = secret; }

    public long getExpiration()                      { return expiration; }
    public void setExpiration(long expiration)       { this.expiration = expiration; }

    public long getRefreshExpiration()               { return refreshExpiration; }
    public void setRefreshExpiration(long v)         { this.refreshExpiration = v; }
}