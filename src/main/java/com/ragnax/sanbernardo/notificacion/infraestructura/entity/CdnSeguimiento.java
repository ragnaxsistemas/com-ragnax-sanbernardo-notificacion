package com.ragnax.sanbernardo.notificacion.infraestructura.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
/**
 *  implementation class for : ProcesoCarta
 * en la base de Datos representa el detalle de las ProcesoCarta
 */
@Entity
@Table (name="cdn_seguimiento_carta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdnSeguimiento {

    @Id
    @Column(name = "codigo_impresion", length = 50, nullable = false)
    private String codigoImpresion;

    @Column(name = "codigo_cd", length = 50, nullable = false)
    private String codigoCd;

    @Column(name = "correlativo_historico", nullable = false)
    private Integer correlativoHistorico;

    @Column(name = "cont_folio_proceso", nullable = false)
    private Integer contFolioProceso;

    @Column(name = "cont_folio_tipo", nullable = false)
    private Integer contFolioTipo;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "rut", length = 12, nullable = false)
    private String rut;

    @Column(name = "patente", length = 10, nullable = false)
    private String patente;

    @Column(name = "cantidad_patente")
    private Integer cantidadPatente = 0;

    @Column(name = "codigo_seguimiento", length = 100, nullable = false)
    private String codigoSeguimiento;

    @Lob
    @Column(name = "archivo_pdf", columnDefinition = "LONGBLOB")
    private byte[] archivoPdf;

    @CreationTimestamp // Maneja automáticamente el DEFAULT CURRENT_TIMESTAMP
    @Column(name = "fecha_registro", updatable = false, nullable = false)
    private LocalDateTime fechaRegistro;

}
