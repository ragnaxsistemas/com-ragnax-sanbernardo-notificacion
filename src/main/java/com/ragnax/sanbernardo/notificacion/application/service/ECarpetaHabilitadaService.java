package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import com.ragnax.sanbernardo.notificacion.application.service.component.MailComponent;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.UnidadDTO;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.EmpresaCliente;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios.EmpresaClienteRepository;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios.UnidadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ECarpetaHabilitadaService {

    private final MailComponent mailComponent;

    private final AFileStorageComponent storageService;

    private final EmpresaClienteRepository empresaClienteRepository;

    private final UnidadRepository unidadRepository;

    public List<UnidadDTO> obtenerUnidadesFront(String codEmpresa) {
        EmpresaCliente empresa = empresaClienteRepository.findByCodigoEmpresaCliente(codEmpresa)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return unidadRepository.findByEmpresaCliente(empresa).stream()
                .filter(u -> {
                    String nombre = u.getShowNombreUnidad().toLowerCase();
                    return nombre.contains("tesoreria") || nombre.contains("juzgado");
                })
                .map(u -> UnidadDTO.builder()
                        .codigoUnidad(u.getCodigoUnidad())
                        .nombreUnidad(u.getNombreUnidad())
                        .showNombreUnidad(u.getShowNombreUnidad())
                        .codEmpresa(empresa.getCodigoEmpresaCliente())
                        .build())
                .toList();
    }

    public Map<String, Object>  habilitarCarpeta(String subPath) throws IOException {

        // 2. Resolver la ruta física usando el StorageService
        // subPath llegará como: "cobranza/tesoreria/CD-FTX_2026..." o "upload/cobranza/tesoreria"
        Path path = storageService.resolveDynamicPath(subPath);

        // LOG de diagnóstico para verificar qué está buscando el servidor
        log.info("Habilitando carpeta para imprenta en: {}" , path.toAbsolutePath());

        if (!Files.exists(path)) {
            return Map.of(
                    "mensaje", "La ruta no existe en el servidor"
            );
        }
        Map<String, Object> carpetaHabilitada = storageService.habilitarImprenta(path);

       // mailComponent.enviarCorreoHabilitarImprenta("observacion", "tipo", "unidad", "nombreArchivo");


        return carpetaHabilitada;
    }

   //enviarCorreoHabilitarImprenta(String observacion, String tipo,  String unidad, int largoCsv, String nombreArchivo)
}

