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
    private String direccion;
    private String comuna;
    private String rol;
    private String anho;
    private String mac;
    private String rut;
    private String ppu;
    private String vehiculo;
    private String fechaInfraccion;
    private String horaInfraccion;
    private String fechaCitacion;
    private String horaCitacion;
    private String codigoInterno;
    private String fechaVencimiento;
    private String folio;


}
