package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.CCorreosToNormalizeService;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarMerge;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RefreshScope
@RestController
@RequestMapping("/procesar-normalizacion")
@CrossOrigin(origins = "*")
public class CCorreosToMergeController {

    private CCorreosToNormalizeService ccorreosToProcessService;

	public CCorreosToMergeController(CCorreosToNormalizeService ccorreosToProcessService) {
		super();
        this.ccorreosToProcessService = ccorreosToProcessService;
	}

    // --- UPLOAD NORMALIZADO: URL http://localhost:9999/notificacion/upload-normalizado/1juzgado ---
    @PostMapping("/from-correos-to-merge/{tipo}/{unidad}")
    public ResponseEntity<?> fromCorreosToMerge(
            @PathVariable String tipo,
            @PathVariable String unidad,
            @RequestParam(value = "user", required = false, defaultValue = "") String user,
            @RequestParam(value = "rutaExcel", required = false, defaultValue = "") String rutaExcelUnion,
            @RequestParam("archivo") MultipartFile fileCorreosCsv) throws Exception {

        // Map to a specific class
        EjecutarMerge ejecutarMerge = EjecutarMerge.builder()
                .tipo(tipo)
                .unidad(unidad)
                .usuarioMerge(user)
                .fileCorreosCsv(fileCorreosCsv)
                .rutaExcelUnion(rutaExcelUnion)
                .build();

        return ccorreosToProcessService.fromCorreosToMerge(ejecutarMerge);

    }
}

