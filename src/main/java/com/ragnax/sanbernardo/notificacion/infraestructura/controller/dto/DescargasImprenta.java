package com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DescargasImprenta implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;
    private String fechaDescarga;
    private String usuarioImprenta;
}
