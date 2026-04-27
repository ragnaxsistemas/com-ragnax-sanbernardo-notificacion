package com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenResponse implements Serializable {

    private String accessToken;
    private Long expiresAt;

    public TokenResponse(String accessToken, Long expiresAt) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }
}
