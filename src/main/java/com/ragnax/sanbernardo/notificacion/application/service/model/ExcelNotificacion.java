package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelNotificacion implements Serializable {

    private String juzgado;
    private String nombreCompleto;
    private String rut;
    private String direccion;
    private String comuna;
    private String anho;
    private String rol;
    private String fechaTramite;
    private String fechaCitacion;
    private String placaPatente;
    private String codigoInterno;
    private String fechaVencimiento;
    private String fechaInfraccion;
    private String horaInfraccion;
    private String folio;

    //private String MAC;
    //rivate String fechaCitacion;
    //private String tipoVehiculo;
    //rivate String fechaCitacion;
    //private String horaCitacion;
    //private String codInterno;



}
