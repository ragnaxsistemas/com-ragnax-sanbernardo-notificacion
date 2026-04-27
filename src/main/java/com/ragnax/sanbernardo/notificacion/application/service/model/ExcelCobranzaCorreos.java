package com.ragnax.sanbernardo.notificacion.application.service.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelCobranzaCorreos implements Serializable {

    @CsvBindByName(column = "clientId")
    private String clientId;

    @CsvBindByName(column = "rut")
    private String rut;

    @CsvBindByName(column = "nombre")
    private String nombre;
    @CsvBindByName(column = "direccion")
    private String direccion;
    @CsvBindByName(column = "comuna")
    private String comuna;
    @CsvBindByName(column = "codigo_seguimiento")
    private String codigo_seguimiento;
    @CsvBindByName(column = "destinatario")
    private String destinatario;
    @CsvBindByName(column = "direccion_original")
    private String direccion_original;
    @CsvBindByName(column = "comuna_propuesta")
    private String comuna_propuesta;
    @CsvBindByName(column = "codigo_postal")
    private String codigo_postal;
    @CsvBindByName(column = "id_sector")
    private String id_sector;
    @CsvBindByName(column = "id_cuartel")
    private String id_cuartel;
    @CsvBindByName(column = "id_cliente")
    private String id_cliente;
    @CsvBindByName(column = "servicio")
    private String servicio;

    public ExcelCobranzaCorreos(String clientId, String rut, String nombre, String direccion, String comuna, String codigo_seguimiento, String destinatario, String direccion_original, String comuna_propuesta, String codigo_postal, String id_sector, String id_cuartel, String id_cliente, String servicio) {
        this.clientId = clientId;
        this.rut = rut;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.codigo_seguimiento = codigo_seguimiento;
        this.destinatario = destinatario;
        this.direccion_original = direccion_original;
        this.comuna_propuesta = comuna_propuesta;
        this.codigo_postal = codigo_postal;
        this.id_sector = id_sector;
        this.id_cuartel = id_cuartel;
        this.id_cliente = id_cliente;
        this.servicio = servicio;
    }
}
