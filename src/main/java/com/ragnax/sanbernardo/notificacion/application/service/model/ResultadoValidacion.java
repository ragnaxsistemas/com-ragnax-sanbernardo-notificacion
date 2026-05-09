package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ResultadoValidacion {

    private List<ExcelCorreos> coincidentes;
    private int noCoincidentes;

    // Constructor, Getters y Setters
    public ResultadoValidacion(List<ExcelCorreos> coincidentes, int noCoincidentes) {
        this.coincidentes = coincidentes;
        this.noCoincidentes = noCoincidentes;
    }
    public List<ExcelCorreos> getCoincidentes() { return coincidentes; }
    public int getNoCoincidentes() { return noCoincidentes; }
}