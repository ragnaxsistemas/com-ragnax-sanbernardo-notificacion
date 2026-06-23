package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor // 2. Add this annotation here
@SuperBuilder(toBuilder = true)
public class ExcelNotificacionMerge extends ExcelNotificacionToNormalize implements Serializable {

    private String codigoSeguimiento;
    private String codigoPostal;
    private String idSector;
    private String idCuartel;
    private String servicio;
    private String destinoClasificacion;

    public ExcelNotificacionMerge(String juzgado, String nombreCompleto, String direccion, String comuna, String rol,
                                  String anho,  String mac, String rut,  String ppu,  String vehiculo,
                                  String fechaInfraccion, String horaInfraccion, String fechaCitacion,  String horaCitacion,  String codigoInterno,
                                  String fechaVencimiento,  String folio, String clientId, String toNormalize,
                                  String codigoSeguimiento, String codigoPostal,String idSector, String idCuartel, String servicio, String destinoClasificacion)
    {
        super(juzgado, nombreCompleto,
                direccion, comuna, rol,  anho,  mac, rut,  ppu,  vehiculo,
                fechaInfraccion, horaInfraccion, fechaCitacion,  horaCitacion,  codigoInterno,
                fechaVencimiento,  folio, clientId, toNormalize);
        this.codigoSeguimiento = codigoSeguimiento;
        this.codigoPostal = codigoPostal;
        this.idSector = idSector;
        this.idCuartel = idCuartel;
        this.servicio = servicio;
        this.destinoClasificacion = destinoClasificacion;
    }

}
