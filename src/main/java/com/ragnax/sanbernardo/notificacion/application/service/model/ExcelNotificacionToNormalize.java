package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ExcelNotificacionToNormalize extends ExcelNotificacion implements Serializable {

    private String clientId;
    private String toNormalize;

    public ExcelNotificacionToNormalize(String juzgado, String nombreCompleto,
        String direccion, String comuna, String rol,  String anho,  String mac, String rut,  String ppu,  String vehiculo,
        String fechaInfraccion, String horaInfraccion, String fechaCitacion,  String horaCitacion,  String codigoInterno,
        String fechaVencimiento,  String folio, String clientId, String toNormalize) {
        super(juzgado, nombreCompleto,
                direccion, comuna, rol,  anho,  mac, rut,  ppu,  vehiculo,
                fechaInfraccion, horaInfraccion, fechaCitacion,  horaCitacion,  codigoInterno,
                fechaVencimiento,  folio);
        this.clientId = clientId;
        this.toNormalize = toNormalize;
    }

}
