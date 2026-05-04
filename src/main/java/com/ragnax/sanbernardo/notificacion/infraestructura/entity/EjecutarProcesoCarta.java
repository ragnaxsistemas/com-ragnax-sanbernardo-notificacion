package com.ragnax.sanbernardo.notificacion.infraestructura.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table (name="ejecutar_proceso_carta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EjecutarProcesoCarta implements Serializable {

    private static final long serialVersionUID = 2840616643537410153L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proceso")
    private Long idProceso;

    @Column(name = "cod_ejecutar_proceso_carta", length = 255)
    private String codEjecutarProcesoCarta;

    @CreationTimestamp // Llena automáticamente la fecha al insertar
    @Column(name = "fecha_registro_creacion_merge", nullable = false, updatable = false)
    private LocalDateTime fechaRegistroCreacionMerge;

    @Column(name = "ejecutado")
    private Boolean ejecutado = false;

    @Column(name = "fecha_registro_ejecucion_merge")
    private LocalDateTime fechaRegistroEjecucionMerge;

    @Column(name = "carpeta_archivo_merge", length = 255)
    private String pathArchivoMerge;

    @Column(name = "tipo_carta", length = 50)
    private String tipoCarta;

    @Column(name = "unidad", length = 50)
    private String unidad;

}
