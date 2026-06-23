package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ExcelCobranzaMerge extends ExcelCobranzaToNormalize implements Serializable {

    private String codigoSeguimiento;
    private String codigoPostal;
    private String idSector;
    private String idCuartel;
    private String servicio;
    private String destinoClasificacion;

    //codigo_postal;id_sector;id_cuartel;id_cliente;servicio;orden_de_impresion;destino_clasificacion

    public ExcelCobranzaMerge(String cert1,
                              String cert2, String fechaCarta, String vence, String folio, String apellidoPaterno, String apellidoMaterno, String nombres, String rut, String dv, String direccion, String comuna, String placaPatente, String dg, String tipoVehiculo, String rolMop, String fechaInfraccion, String horaInfraccion, String convenio1, String convenio2, String codigoBarra, String valorMulta, String lugarMulta, String fechaCitacion, String juzgado, String piso, String clientId, String toNormalize,
                              String codigoSeguimiento, String codigoPostal,String idSector, String idCuartel, String servicio, String destinoClasificacion) {
        super(cert1, cert2, fechaCarta, vence, folio, apellidoPaterno, apellidoMaterno, nombres, rut, dv, direccion, comuna, placaPatente, dg, tipoVehiculo, rolMop, fechaInfraccion, horaInfraccion, convenio1, convenio2, codigoBarra, valorMulta, lugarMulta, fechaCitacion, juzgado, piso, clientId, toNormalize);
        this.codigoSeguimiento = codigoSeguimiento;
        this.codigoPostal = codigoPostal;
        this.idSector = idSector;
        this.idCuartel = idCuartel;
        this.servicio = servicio;
        this.destinoClasificacion = destinoClasificacion;
    }
}
