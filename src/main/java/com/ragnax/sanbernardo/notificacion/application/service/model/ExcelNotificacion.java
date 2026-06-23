package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
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

    public ExcelNotificacion(String juzgado, String nombreCompleto, String direccion, String comuna, String rol, String anho, String mac, String rut, String ppu, String vehiculo, String fechaInfraccion, String horaInfraccion, String fechaCitacion, String horaCitacion, String codigoInterno, String fechaVencimiento, String folio) {
        this.juzgado = juzgado;
        this.nombreCompleto = nombreCompleto;
        this.direccion = direccion;
        this.comuna = comuna;
        this.rol = rol;
        this.anho = anho;
        this.mac = mac;
        this.rut = rut;
        this.ppu = ppu;
        this.vehiculo = vehiculo;
        this.fechaInfraccion = fechaInfraccion;
        this.horaInfraccion = horaInfraccion;
        this.fechaCitacion = fechaCitacion;
        this.horaCitacion = horaCitacion;
        this.codigoInterno = codigoInterno;
        this.fechaVencimiento = fechaVencimiento;
        this.folio = folio;
    }
}
