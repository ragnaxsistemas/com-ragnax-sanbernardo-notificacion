package com.ragnax.sanbernardo.notificacion.infraestructura.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ragnaxsistemas on 30-20-2020.
 */

@Configuration
public class OpenApiConfig {

	@Value("${swagger.configuracion.empresainfo.nombre}")
	private String nombreEmpresa;
	
	@Value("${swagger.configuracion.empresainfo.url}")
	private String urlEmpresa;

	@Value("${swagger.configuracion.empresainfo.email}")
	private String emailEmpresa;

	@Value("${swagger.configuracion.apiinfo.titulo}")
	private String apiinfoTitulo;

	@Value("${swagger.configuracion.apiinfo.descripcion}")
	private String apiinfoDescripcion;

	@Value("${swagger.configuracion.apiinfo.version}")
	private String apiinfoVersion;

	@Value("${swagger.configuracion.apiinfo.termsOfServiceUrl}")
	private String apiinfoTermsOfServiceUrl;

	@Value("${swagger.configuracion.apiinfo.license}")
	private String apiinfoLicense;

	@Value("${swagger.configuracion.apiinfo.licenseUrl}")
	private String apiinfoLicenseUrl;
	
	@Bean
    public OpenAPI customOpenAPI() {
		
		return new OpenAPI()
                .components(new Components())
                .info(new Info().title(apiinfoTitulo).description(
                		apiinfoDescripcion).termsOfService(apiinfoTermsOfServiceUrl).version(apiinfoVersion)
                		.contact(generarContact()).license(generarLicense())
                		);
    }
	
	public Contact generarContact() {
		Contact contact = new Contact();
		contact.setName(nombreEmpresa);
		contact.setUrl(urlEmpresa);
		contact.setEmail(emailEmpresa);
		return contact;
	}
	
	public License generarLicense() {
		License license = new License();
		license.setName(apiinfoLicense);
		license.setUrl(apiinfoLicenseUrl);
		return license;
	}
}