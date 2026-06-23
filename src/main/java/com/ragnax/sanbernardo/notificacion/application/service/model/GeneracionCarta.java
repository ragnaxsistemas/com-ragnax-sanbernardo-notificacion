package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneracionCarta implements Serializable {

    private String procesoGeneracionCarta;
    /****numFolio contador del Proceso comienza 1**/
    private Integer contFolioTipo;
    private Integer contFolioProceso;
    private Integer contTipoCartas;
    private Long correlativoHistorico;

    private List<byte[]> listaPdfs;
    private List<ExcelCobranzaImpresion> listaExcelCobranzaImpresion;
    private List<ExcelNotificacionImpresion> listaExcelNotificacionImpresion;

    private List<ExcelCobranzaBD> listaExcelCobranzaBD;
    private List<ExcelNotificacionBD> listaExcelNotificacionBD;
}
