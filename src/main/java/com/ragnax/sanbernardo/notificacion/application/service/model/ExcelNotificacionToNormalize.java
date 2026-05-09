package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelNotificacionToNormalize extends ExcelNotificacion implements Serializable {

    private String clientId;
    private String toNormalize;

    public ExcelNotificacionToNormalize(String juzgado, String nombreCompleto, String rut, String direccion, String comuna, String anho, String rol, String fechaTramite, String fechaCitacion, String placaPatente, String codigoInterno, String fechaVencimiento, String fechaInfraccion, String horaInfraccion, String folio, String clientId, String toNormalize) {
        super(juzgado, nombreCompleto, rut, direccion, comuna, anho, rol, fechaTramite, fechaCitacion, placaPatente, codigoInterno, fechaVencimiento, fechaInfraccion, horaInfraccion, folio);
        this.clientId = clientId;
        this.toNormalize = toNormalize;
    }

}
