package com.ragnax.sanbernardo.notificacion.application.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EjecutarCartas extends EjecutarMerge implements Serializable {

    private long correlativoInicio;
    private long correlativoHistorico;

    private String totalCartas; //Total filas en Merge
    private String totalIndividuales; //Total filas en Merge
    private String totalMasivas; //Total filas en Merge
    private String totalErroneas; //Total filas en Merge
    //-
    private String totalUTM;
    private String totalRuts;
    private String totalPatentesUnicas;

    //private Boolean activarConsolidadoImprenta;

    //private MultipartFile fileCorreosCsv;
    //private List<ExcelCobranzaNormalizado> listaExcelCobranzaNormalizado;
    //private List<byte[]> listaPdfs;

}
