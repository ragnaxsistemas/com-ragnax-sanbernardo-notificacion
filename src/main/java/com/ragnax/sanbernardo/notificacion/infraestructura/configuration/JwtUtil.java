package com.ragnax.sanbernardo.notificacion.infraestructura.configuration;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.LoginResponse;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.TokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.serializerByType(
                BigDecimal.class,
                ToStringSerializer.instance
        );
    }

    private static final String SECRET = "mi_clave_super_segura_12345678901234567890";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXPIRATION = 600000;

    public static TokenResponse generateToken(
            LoginResponse login
    ){

        long now = System.currentTimeMillis();
        long expiresAt = now + EXPIRATION;

        Map<String, Object> unidadNegocioMap = Map.of(
                "codigoUnidad", login.getUnidad().getCodigoUnidad(),
                "nombreUnidad", login.getUnidad().getNombreUnidad(),
                "showNombreUnidad", login.getUnidad().getShowNombreUnidad()
                // Agrega aquí solo lo que necesites, excluyendo el ID
        );

        Map<String, Object> empresaMap = Map.of(
                "codigoEmpresaCliente", login.getEmpresa().getCodigoEmpresaCliente(),
                "rutEmpresaCliente", login.getEmpresa().getRutEmpresaCliente(),
                "razonSocialEmpresaCliente", login.getEmpresa().getNombreRazonSocialEmpresaCliente(),
                "nombreEmpresaCliente", login.getEmpresa().getNombreEmpresaCliente()
                // Agrega aquí solo lo que necesites, excluyendo el ID
        );


        Map<String, Object> roleMap = Map.of(
                "nombre", login.getRole().getNombre()
        );

        String token = Jwts.builder()
                .subject(login.getUsername())
                .claims(Map.of(
                        "nombre", login.getNombreMember(),
                        "apellidoPaterno", login.getApellidoPaternoMember(),
                        "apellidoMaterno", login.getApellidoMaternoMember(),
                        "telefono", login.getTelefonoContactoMember(),
                        "email", login.getEmailPerfil(),
                        "unidadNegocio", unidadNegocioMap,
                        "empresa", empresaMap,
                        "role", roleMap,
                        "menus", login.getItems()
                ))
                .issuedAt(new Date(now))
                .expiration(new Date(expiresAt))
                .signWith(KEY)
                .compact();

        return new TokenResponse(token, expiresAt);
    }
}