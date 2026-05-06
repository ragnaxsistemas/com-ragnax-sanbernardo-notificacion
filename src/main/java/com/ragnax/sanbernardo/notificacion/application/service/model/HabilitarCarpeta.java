package com.ragnax.sanbernardo.notificacion.application.service.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabilitarCarpeta {

    private String tipoUnidad;

    private LocalDateTime fechaHabilitacion;

    private String ubicacion;

}