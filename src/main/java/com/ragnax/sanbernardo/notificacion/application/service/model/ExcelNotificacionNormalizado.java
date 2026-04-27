package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelNotificacionNormalizado extends ExcelNotificacion implements Serializable {

    private String clientId;
    private String toNormalize;

    public ExcelNotificacionNormalizado(String juzgado, String nombre, String direccion, String comuna, String rol, String anho, String MAC, String rut, String placaPatente, String tipoVehiculo, String fechaInfraccion, String horaInfraccion, String fechaCitacion, String horaCitacion, String codInterno, String vence, String folio, String clientId, String toNormalize) {
        super(juzgado, nombre, direccion, comuna, rol, anho, MAC, rut, placaPatente, tipoVehiculo, fechaInfraccion, horaInfraccion, fechaCitacion, horaCitacion, codInterno, vence, folio);
        this.clientId = clientId;
        this.toNormalize = toNormalize;
    }
}
