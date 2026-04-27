package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EjecutarUpload implements Serializable {

    private String tipo;
    private String unidad;
    private String fechaCreacionUpload;
    private String pathCarpetaUpload;
    private String usuarioUpload;

    private String observacion;

    private String valor;

    private String baseNombre;
    private String pathArchivoUpload;
    private String sizeArchivoUpload;
    private String nombreArchivoUpload;
    private String pathArchivoBackup;
    private String pathArchivoNormalizado;
    /***Al Correo **/
    private String sizeArchivoNormalizado;
    private String archivoCsvToNormalize;
    private String registrosUnicos; //Cuantos patente - rut existen
    /***Setear Nulo*/
    private byte[] contenidoCsv;
    private MultipartFile file;
    /*****/




}
