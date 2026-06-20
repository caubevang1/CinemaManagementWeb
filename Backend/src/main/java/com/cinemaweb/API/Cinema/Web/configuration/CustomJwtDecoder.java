package com.cinemaweb.API.Cinema.Web.configuration;


import com.cinemaweb.API.Cinema.Web.service.AuthenticationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @PostConstruct
    private void init() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        // Nimbus verify chữ ký + hạn (ném BadJwtException nếu sai).
        Jwt jwt = nimbusJwtDecoder.decode(token);

        // Chỉ chấp nhận access token và token chưa bị blacklist (đã logout).
        if (!AuthenticationService.TOKEN_TYPE_ACCESS.equals(jwt.getClaimAsString("type")))
            throw new BadJwtException("Token invalid");

        if (authenticationService.isAccessTokenBlacklisted(jwt.getId()))
            throw new BadJwtException("Token invalid");

        return jwt;
    }
}
