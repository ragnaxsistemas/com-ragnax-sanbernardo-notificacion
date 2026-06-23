package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class ExcelNotificacionBD extends ExcelNotificacionImpresion implements Serializable {

    private String nombreArchivo;
    private String codigoCd;
    private String cantidadPatente;
    private String html;
    private byte[] pdf;
    private String bloqueActualExcelNotificacion;

}

