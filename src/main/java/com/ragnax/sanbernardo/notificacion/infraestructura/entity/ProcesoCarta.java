package com.ragnax.sanbernardo.notificacion.infraestructura.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  implementation class for : ProcesoCarta
 * en la base de Datos representa el detalle de las ProcesoCarta
 */
@Entity
@Table (name="proceso_notificacion_carta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoCarta implements Serializable{

	private static final long serialVersionUID = 2840616643537410153L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@OrderBy
	@Column(name="id_proceso")
	private Integer idProceso;

    @Column(name="fecha_registro")
	private LocalDateTime fechaRegistro;
	
	@Column(name="inicio_correlativo")
	private Long inicioCorrelativo;

    @Column(name="ultimo_correlativo")
    private Long ultimoCorrelativo;

    @Column(name="usuario_proceso")
    private String usuarioProceso;

    @Column(name="path_origen")
    private String pathOrigen;

    @Column(name="path_destino")
    private String pathDestino;

    @Column(name="proceso")
    private String proceso;

    @Column(name="tipo_carta")
    private String tipoCarta;

    public ProcesoCarta(LocalDateTime fechaRegistro, Long inicioCorrelativo, Long ultimoCorrelativo,
                        String usuarioProceso,
                        String pathOrigen,
                        String pathDestino,
                        String proceso,
                        String tipoCarta) {
        this.fechaRegistro = fechaRegistro;
        this.inicioCorrelativo = inicioCorrelativo;
        this.ultimoCorrelativo = ultimoCorrelativo;
        this.usuarioProceso = usuarioProceso;
        this.pathOrigen = pathOrigen;
        this.pathDestino = pathDestino;
        this.proceso = proceso;
        this.tipoCarta = tipoCarta;
    }
}
