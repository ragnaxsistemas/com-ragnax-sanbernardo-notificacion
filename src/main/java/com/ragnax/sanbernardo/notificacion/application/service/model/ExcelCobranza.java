package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelCobranza implements Serializable {

    private String cert1;
    private String cert2;
    private String fechaCarta;
    private String vence;
    private String folio;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private String rut;
    private String dv;
    private String direccion;
    private String comuna;
    private String placaPatente;
    private String dg;
    private String tipoVehiculo;
    private String rolMop;
    private String fechaInfraccion;
    private String horaInfraccion;
    private String convenio1;
    private String convenio2;
    private String codigoBarra;
    private String valorMulta;
    private String lugarMulta;
    private String fechaCitacion;
    private String juzgado;
    private String piso;

    public ExcelCobranza(String cert1, String cert2, String fechaCarta, String vence, String folio, String apellidoPaterno, String apellidoMaterno, String nombres, String rut, String dv, String direccion, String comuna, String placaPatente, String dg, String tipoVehiculo, String rolMop, String fechaInfraccion, String horaInfraccion, String convenio1, String convenio2, String codigoBarra, String valorMulta
            ,String lugarMulta ,String fechaCitacion, String juzgado, String ubicacion) {
        this.cert1 = cert1;
        this.cert2 = cert2;
        this.fechaCarta = fechaCarta;
        this.vence = vence;
        this.folio = folio;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nombres = nombres;
        this.rut = rut;
        this.dv = dv;
        this.direccion = direccion;
        this.comuna = comuna;
        this.placaPatente = placaPatente;
        this.dg = dg;
        this.tipoVehiculo = tipoVehiculo;
        this.rolMop = rolMop;
        this.fechaInfraccion = fechaInfraccion;
        this.horaInfraccion = horaInfraccion;
        this.convenio1 = convenio1;
        this.convenio2 = convenio2;
        this.codigoBarra = codigoBarra;
        this.valorMulta = valorMulta;
        this.lugarMulta = lugarMulta;
        this.fechaCitacion = fechaCitacion;
        this.juzgado = juzgado;
        this.piso = ubicacion;
    }
}
