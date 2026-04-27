package com.ragnax.sanbernardo.notificacion.application.service.model.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ImsbException extends RuntimeException {

    private final HttpStatus status;
    private final String detalle;


    public ImsbException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
        this.detalle = null;
    }

    public ImsbException(String mensaje, String detalle, HttpStatus status) {
        super(mensaje);
        this.detalle = detalle;
        this.status = status;
    }
}