package com.ragnax.sanbernardo.notificacion.infraestructura.exception;

import java.io.Serializable;

public class ProcesoNotificacionCartaException extends RuntimeException implements Serializable{

	private static final long serialVersionUID = 1480116895834882204L;
	
	private String codigoSSOProcesoNotificacionCartaException;

	public ProcesoNotificacionCartaException(){
		super();
	}

	public ProcesoNotificacionCartaException(String message){
		super(message);
	}
	
	public ProcesoNotificacionCartaException(String codigoSSOProcesoNotificacionCartaException, String message){
		super(message);
		this.codigoSSOProcesoNotificacionCartaException = codigoSSOProcesoNotificacionCartaException;
	}
	
	public ProcesoNotificacionCartaException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ProcesoNotificacionCartaException(Throwable cause){
		super(cause);
	}

	public String getCodigoSSOProcesoNotificacionCartaException() {
		return codigoSSOProcesoNotificacionCartaException;
	}

	public void setCodigoSSOProcesoNotificacionCartaException(String codigoSSOProcesoNotificacionCartaException) {
		this.codigoSSOProcesoNotificacionCartaException = codigoSSOProcesoNotificacionCartaException;
	}
	
}
