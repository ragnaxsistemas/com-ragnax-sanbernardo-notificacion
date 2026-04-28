package com.ragnax.sanbernardo.notificacion.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ResultadoValidacion {
    private List<ExcelCobranzaCorreos> coincidentes;
    private int noCoincidentes;

    // Constructor, Getters y Setters
    public ResultadoValidacion(List<ExcelCobranzaCorreos> coincidentes, int noCoincidentes) {
        this.coincidentes = coincidentes;
        this.noCoincidentes = noCoincidentes;
    }
    public List<ExcelCobranzaCorreos> getCoincidentes() { return coincidentes; }
    public int getNoCoincidentes() { return noCoincidentes; }
}