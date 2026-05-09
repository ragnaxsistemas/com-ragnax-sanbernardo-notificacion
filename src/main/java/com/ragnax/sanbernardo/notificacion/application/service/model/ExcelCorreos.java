package com.ragnax.sanbernardo.notificacion.application.service.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class ExcelCorreos implements Serializable {

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
    @CsvBindByName(column = "toNormalize")
    private String toNormalize;
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
}
