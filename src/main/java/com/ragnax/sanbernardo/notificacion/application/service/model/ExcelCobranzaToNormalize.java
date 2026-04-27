package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelCobranzaToNormalize extends ExcelCobranza implements Serializable {

    private String clientId;
    private String toNormalize;

    public ExcelCobranzaToNormalize(String cert1, String cert2, String fechaCarta, String vence, String folio, String apellidoPaterno, String apellidoMaterno, String nombres, String rut, String dv, String direccion, String comuna, String placaPatente, String dg, String tipoVehiculo, String rolMop, String fechaInfraccion, String horaInfraccion, String convenio1, String convenio2, String codigoBarra, String valorMulta
            , String lugarMulta, String fechaCitacion, String juzgado, String piso, String clientId, String toNormalize) {
        super(cert1, cert2, fechaCarta, vence, folio, apellidoPaterno, apellidoMaterno, nombres, rut, dv, direccion, comuna, placaPatente, dg, tipoVehiculo, rolMop, fechaInfraccion, horaInfraccion, convenio1, convenio2, codigoBarra, valorMulta, lugarMulta, fechaCitacion, juzgado, piso);
        this.clientId = clientId;
        this.toNormalize = toNormalize;
    }
}
