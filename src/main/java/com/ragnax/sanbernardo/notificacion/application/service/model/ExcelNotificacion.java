package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelNotificacion implements Serializable {

    private String juzgado;
    private String nombre;
    private String direccion;
    private String comuna;

    private String rol;
    private String anho;
    private String MAC;

    private String rut;
    private String placaPatente;
    private String tipoVehiculo;
    private String fechaInfraccion;
    private String horaInfraccion;
    private String fechaCitacion;
    private String horaCitacion;
    private String codInterno;
    private String vence;
    private String folio;

    public ExcelNotificacion(String juzgado, String nombre, String direccion, String comuna, String rol, String anho, String MAC, String rut, String placaPatente, String tipoVehiculo, String fechaInfraccion, String horaInfraccion, String fechaCitacion, String horaCitacion, String codInterno, String vence, String folio) {
        this.juzgado = juzgado;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.rol = rol;
        this.anho = anho;
        this.MAC = MAC;
        this.rut = rut;
        this.placaPatente = placaPatente;
        this.tipoVehiculo = tipoVehiculo;
        this.fechaInfraccion = fechaInfraccion;
        this.horaInfraccion = horaInfraccion;
        this.fechaCitacion = fechaCitacion;
        this.horaCitacion = horaCitacion;
        this.codInterno = codInterno;
        this.vence = vence;
        this.folio = folio;
    }
}
