package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EjecutarMerge extends EjecutarUpload implements Serializable {


   /***private String tipo;
    private String unidad;
    private String fechaCreacionUpload;
    private String pathCarpeta;
    private String usuarioUpload;

    private String observacion;

    private String valor;

    private String baseNombre;
    private String pathArchivoUpload;
    private String nombreArchivoUpload;
    private String pathArchivoBackup;
    private String pathArchivoNormalizado;
    private String totalFilasGeneradasCsv;

    private MultipartFile file;***/
    private String rutaExcelUnion;

    private String fechaCreacionMerge;
    private String usuarioMerge;
    private String pathArchivoMerge;
    private String nombreArchivoMerge;

    private String pathFolderCartas; //Cobranza -  Norificacion
    private String pathArchivoCartas; //Cobranza -  Norificacion
    private String pathArchivoConsolidado; //Cobranza -  Norificacion
    private String pathReporte;

    private long correlativoInicio;
    private long correlativoHistorico;

    private String totalFilasGeneradasExcel; //Total filas en Merge
    private String totalCartas; //Total filas en Merge
    private String totalIndividuales; //Total filas en Merge
    private String totalMasivas; //Total filas en Merge
    private String totalErroneas; //Total filas en Merge
    //-
    private String totalUTM;
    private String totalRuts;
    private String totalPatentesUnicas;


    private MultipartFile fileCorreosCsv;
    private List<ExcelCobranzaNormalizado> listaExcelCobranzaNormalizado;
    private List<byte[]> listaPdfs;

}
