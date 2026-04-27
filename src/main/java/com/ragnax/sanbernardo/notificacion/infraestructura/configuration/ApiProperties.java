package com.ragnax.sanbernardo.notificacion.infraestructura.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
/****Properties que pueden cambiar el valor ****/
public class ApiProperties {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${archivo.excel.nombreCarpetaUpload}")
    private String archivoExcelNombreCarpetaUpload;

    @Value("${archivo.excel.nombreCarpetaNormalizada}")
    private String archivoExcelNombreCarpetaNormalizada;

    @Value("${archivo.excel.nombreCarpetaMerge}")
    private String archivoExcelNombreCarpetaMerge;

    @Value("${archivo.excel.nombreArchivoNormalizadaCobranza}")
    private String archivoExcelNombreArchivoNormalizadaCobranza;

    @Value("${archivo.excel.nombreArchivoNormalizadaCobranzaCorreos}")
    private String archivoExcelNombreArchivoNormalizadaCobranzaCorreos;

    @Value("${archivo.excel.nombreArchivoMergeCobranza}")
    private String archivoExcelNombreArchivoMergeCobranza;




    @Value("${archivo.excel.nombreHojaUploadCobranza}")
    private String archivoExcelNombreHojaCobranza;

    @Value("${archivo.excel.nombreHojaNormalizadaCobranza}")
    private String archivoExcelNombreHojaNormalizadaCobranza;

   // @Value("${archivo.excel.nombreHojaCobranzaNormalizada}")
   // private String archivoExcelNombreHojaCobranzaNormalizada;

    @Value("${archivo.excel.nombreHojaMergeCobranza}")
    private String archivoExcelNombreHojaMergeCobranza;

    @Value("${archivo.excel.nombreHojaNotificacion}")
    private String archivoExcelNombreHojaNotificacion;

    @Value("${archivo.html.nombreCarpetaTemplate}")
    private String archivoHtmlNombreCarpetaTemplate;

    @Value("${archivo.html.nombreArchivoCobranzaIndividual}")
    private String archivoHtmlNombreArchivoCobranzaIndividual;

    @Value("${archivo.html.nombreArchivoCobranzaMasiva}")
    private String archivoHtmlNombreArchivoCobranzaMasiva;

    @Value("${archivo.html.nombreArchivoNotificacionV2}")
    private String archivoHtmlNombreArchivoNotificacionIndividual;

    @Value("${archivo.html.nombreArchivoNotificacionV2}")
    private String archivoHtmlNombreArchivoNotificacionMasiva;

    @Value("${archivo.html.nombreArchivoReporte}")
    private String archivoHtmlNombreArchivoReporte;

    @Value("${archivo.html.logoEscudo}")
    private String archivoHtmlLogoEscudo;

    @Value("${archivo.html.logoFirma1Juzgado}")
    private String archivoHtmlLogoFirma1Juzgado;

    @Value("${archivo.html.logoFirma2Juzgado}")
    private String archivoHtmlLogoFirma2Juzgado;

    @Value("${archivo.html.logoFirmaTesoreria}")
    private String archivoHtmlLogoFirmaTesoreria;

    @Value("${archivo.html.logoBciCupon}")
    private String archivoHtmlLogoBciCupon;


    @Value("${archivo.html.logo}")
    private String archivoHtmlLogo;


    @Value("${archivo.creacion.carpeta}")
    private String archivoCreacionCarpeta;


    @Value("${archivo.creacion.respaldo}")
    private String archivoCreacionRespaldo;

    @Value("${archivo.creacion.respaldoNormalizado}")
    private String archivoCreacionRespaldoNormalizado;

    @Value("${archivo.creacion.adjuntos.subCarpetaNormalizado}")
    private String archivoCreacionAdjuntoSubCarpetaNormalizado;

    @Value("${archivo.creacion.adjuntos.subCarpetaMerge}")
    private String archivoCreacionAdjuntoSubCarpetaMerge;

 //   @Value("${archivo.creacion.adjuntos.subCarpetaNormalizadoDocumento}")
 //   private String archivoCreacionAdjuntoSubCarpetaDesnormalizadoDocumento;

    /***@Value("${archivo.creacion.adjuntos.subCarpetaNormalizadoCorreos}")
    private String archivoCreacionAdjuntoSubCarpetaDesnormalizadoCorreos;***/

    /***@Value("${archivo.creacion.adjuntos.subCarpetaMerge}")
    private String archivoCreacionAdjuntoSubCarpetaNormalizado;***/

    /***@Value("${archivo.creacion.adjuntos.subCarpetaMergeDocumento}")
    private String archivoCreacionAdjuntoSubCarpetaNormalizadoDocumento;***/

    /***@Value("${archivo.creacion.adjuntos.subCarpetaMergeCorreos}")
    private String archivoCreacionAdjuntoSubCarpetaNormalizadoCorreos;***/

    /***@Value("${archivo.creacion.adjuntos.subCarpetaProcesado}")
    private String archivoCreacionAdjuntoSubCarpetaProcesado;***/

    /*******************************************************/
    /*******************************************************/
    /*******************************************************/

    @Value("${archivo.creacion.adjuntos.subCarpetaCobranza}")
    private String archivoCreacionAdjuntoSubCarpetaCobranza;

    @Value("${archivo.creacion.adjuntos.subCarpetaCobranzaDocumento}")
    private String archivoCreacionAdjuntoSubCarpetaCobranzaDocumento;

    @Value("${archivo.creacion.adjuntos.subCarpetaCobranzaConsolidado}")
    private String archivoCreacionAdjuntoSubCarpetaCobranzaConsolidado;

    @Value("${archivo.creacion.adjuntos.subCarpetaCobranzaReporte}")
    private String archivoCreacionAdjuntoSubCarpetaCobranzaReporte;

    @Value("${archivo.creacion.adjuntos.subCarpetaNotificacion}")
    private String archivoCreacionAdjuntoSubCarpetaNotificacion;

    @Value("${archivo.creacion.adjuntos.subCarpetaNotificacionDocumento}")
    private String archivoCreacionAdjuntoSubCarpetaNotificacionDocumento;

    @Value("${archivo.creacion.adjuntos.subCarpetaNotificacionConsolidado}")
    private String archivoCreacionAdjuntoSubCarpetaNotificacionConsolidado;

    @Value("${archivo.creacion.adjuntos.subCarpetaNotificacionReporte}")
    private String archivoCreacionAdjuntoSubCarpetaNotificacionReporte;

    @Value("${archivo.creacion.adjuntos.nombreArchivoConsolidado}")
    private String archivoCreacionAdjuntoNombreArchivoConsolidado;

    @Value("${archivo.creacion.adjuntos.nombreArchivoReporte}")
    private String archivoCreacionAdjuntoNombreArchivoReporte;

}