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

    //private AFileStorageComponent storageService;

    private CCorreosToNormalizeService ccorreosToProcessService;

	public CCorreosToMergeController(CCorreosToNormalizeService ccorreosToProcessService) {
		super();
        this.ccorreosToProcessService = ccorreosToProcessService;
	}

    // --- UPLOAD NORMALIZADO: URL http://localhost:9999/notificacion/upload-normalizado/1juzgado ---
    @PostMapping("/to-unnorm-to-merge/{tipo}/{unidad}")
    public ResponseEntity<?> toUnnormToMerge(
            @PathVariable String tipo,
            @PathVariable String unidad,
            @RequestParam(value = "user", required = false, defaultValue = "") String user,
           // @RequestParam(value = "observacion", required = false, defaultValue = "") String observacion,
           // @RequestParam(value = "ejecutarNotificacion", required = false, defaultValue = "") String sejecutarNotificacion,
            @RequestParam("archivo") MultipartFile fileCorreosCsv) throws Exception {

        // Map to a specific class
        EjecutarMerge ejecutarMerge = EjecutarMerge.builder()
                .tipo(tipo)
                .unidad(unidad)
                .usuarioMerge(user)
                .fileCorreosCsv(fileCorreosCsv)
                .build();

        return ccorreosToProcessService.procesarToUnnormToMerge(ejecutarMerge);

    }
}

        /***ObjectMapper mapper = new ObjectMapper();
// Map to a specific class
        EjecutarUpload ejecutarNotificacion = mapper.readValue(sejecutarNotificacion, EjecutarUpload.class);

        //subir Archivo del csv
        ResponseEntity<?> map = storageService.procesarSubida("merge", ejecutarNotificacion.getTipo(),
                ejecutarNotificacion.getUnidad(), user,
                observacion,
                storageService.validarArchivo(file));

        ccorreosToProcessService.executeToUnnormToMerge(ejecutarNotificacion,
                observacion,
                user,
                file);

        return map.ok().build();***/
    //}

	/***************************************************/
	/*************** Cobranzas *******************/
	/***************************************************/
	/***@PostMapping(value =  "/ejecutar-archivo-correos-cobranza", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void>  ejecutarArchivoCobranza(
	@RequestBody @Valid EjecutarNotificacion ejecutarNotificacion)  {

        ccorreosToProcessService.execute(ejecutarNotificacion);

        return ResponseEntity.ok().build();
	}

    /***************************************************/
    /*************** Notificaciones -1er 2do Jzdo *******************/
    /***************************************************/
    /***@PostMapping(value =  "/ejecutar-archivo-correos-notificacion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void>  ejecutarArchivoNotificacion(
            @RequestBody @Valid EjecutarNotificacion ejecutarNotificacion)  {

        ccorreosToProcessService.execute(ejecutarNotificacion);

        return ResponseEntity.ok().build();
    }---/
}***/
