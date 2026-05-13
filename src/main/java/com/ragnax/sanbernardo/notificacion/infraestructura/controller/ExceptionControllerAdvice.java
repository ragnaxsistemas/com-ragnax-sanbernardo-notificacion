package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.model.ApiRagnaxError;
import com.ragnax.sanbernardo.notificacion.infraestructura.exception.ProcesoNotificacionCartaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ragnaxsistemas on 30-20-2020.
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class ExceptionControllerAdvice {
	

	@ResponseStatus(value = HttpStatus.ACCEPTED)
	@ExceptionHandler(ProcesoNotificacionCartaException.class)
	public ResponseEntity<ApiRagnaxError> handlerException(ProcesoNotificacionCartaException sae) {
		log.error("Error ProcesoNotificacionCartaException {}", sae.getCodigoSSOProcesoNotificacionCartaException(), sae.getMessage());

		return new ResponseEntity<>(new ApiRagnaxError(sae.getCodigoSSOProcesoNotificacionCartaException(), sae.getMessage()),
				HttpStatus.ACCEPTED);
	}
	
	@ResponseStatus(value = HttpStatus.ALREADY_REPORTED)
	@ExceptionHandler(NumberFormatException.class)
	public ResponseEntity<ApiRagnaxError> handlerException(NumberFormatException nfe) {
		log.error("Error NumberFormatException {}", nfe.getMessage());

		return new ResponseEntity<>(new ApiRagnaxError("Error en Formato", nfe.getMessage()),
				HttpStatus.ALREADY_REPORTED);
	}
	
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiRagnaxError> handlerException(Exception e) {
		log.error("Error Exception {}", e.getMessage());
		return new ResponseEntity<>(new ApiRagnaxError("Error en Aplicacion",  e.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}