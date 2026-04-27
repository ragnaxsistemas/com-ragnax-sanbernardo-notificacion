package com.ragnax.sanbernardo.notificacion.application.service.utilidades;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PlantillaCargar {

    public static String cargarPlantilla(String lecturaHtml) throws Exception {

        ClassPathResource resource = new ClassPathResource(lecturaHtml);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
