/***package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.CCorreosToNormalizeService;
import com.ragnax.sanbernardo.notificacion.application.service.DProcesarCartaCobranzaService;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarMerge;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarUpload;
import jakarta.validation.Valid;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RefreshScope
@RestController
@RequestMapping("/procesar")
@CrossOrigin(origins = "*")
public class DProcesarCartaController {

    private CCorreosToNormalizeService ccorreosToProcessService;
	private DProcesarCartaCobranzaService dprocesarCartaCobranzaService;

	public DProcesarCartaController(CCorreosToNormalizeService ccorreosToProcessService,
                                    DProcesarCartaCobranzaService dprocesarCartaCobranzaService) {
		super();
        this.ccorreosToProcessService = ccorreosToProcessService;
		this.dprocesarCartaCobranzaService = dprocesarCartaCobranzaService;
	}

	/***************************************************/
	/*************** Cobranzas *******************/
	/***************************************************/
	/***@PostMapping(value =  "/execute-archivo-cobranza", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> executeArchivoCobranza(
	@RequestBody @Valid EjecutarMerge ejecutarMerge)  {

        return dprocesarCartaCobranzaService.executeArchivoCobranza(ejecutarMerge);

	}***/

    /***************************************************/
    /*************** Notificaciones -1er 2do Jzdo ******/
    /************************************************
    @PostMapping(value =  "/ejecutar-archivo-notificacion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void>  ejecutarArchivoNotificacion(
            @RequestBody @Valid EjecutarNotificacion ejecutarNotificacion)  {

        cartaNotificacionService.execute(ejecutarNotificacion);

        return ResponseEntity.ok().build();
    }---/
}***/
