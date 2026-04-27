package com.ragnax.sanbernardo.notificacion.application.service.component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class MailComponent {

    @Autowired
    private JavaMailSender emailSender;

    public  void enviarCorreoResend(String destinatario, String observacion, int largoCsv, byte[] archivoAdjunto, String nombreArchivo) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Si aún no has validado tu dominio, usa el de prueba de Resend:
            helper.setFrom("onboarding@resend.dev", "Generacion Cartas Ilustre Municipalidad San bernardo");
           // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo(destinatario);
            helper.setSubject(String.format("Solicitud normalizacion %s Correos de Chile - I. Municipalidad de San Bernardo", observacion));

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimada Fernanda,\n" +
                            "Buen día, favor solicito normalizar archivo adjunto correspondiente a %d registros del %s de la I. Municipalidad de San Bernardo.\n\n" +
                            "Quedamos atentos a sus comentarios, saludos cordiales y gracias de antemano,\n\n" +
                            "Julio Cornejo\n" +
                            "Cel. 993003452",
                    largoCsv,
                    observacion
            );

            helper.setText(cuerpo);

            // Adjuntar el archivo
            helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));// 'true' para HTML (como tus plantillas)

            emailSender.send(message);
            System.out.println("Correo enviado con éxito vía Resend");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public void enviarCorreoNormalizacion(String destinatario, String observacion, int largoCsv, byte[] archivoAdjunto, String nombreArchivo) throws MessagingException, MessagingException {

        MimeMessage message = emailSender.createMimeMessage();

        // El parámetro 'true' indica que el mensaje es multipart (soporta adjuntos)
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        // Configuración de Cabeceras
        helper.setTo(destinatario);
        helper.setSubject(String.format("Solicitud normalizacion %s Correos de Chile - I. Municipalidad de San Bernardo", observacion));

        // Construcción del Cuerpo
        String cuerpo = String.format(
                "Estimada Fernanda,\n" +
                        "Buen día, favor solicito normalizar archivo adjunto correspondiente a %d registros del %s de la I. Municipalidad de San Bernardo.\n\n" +
                        "Quedamos atentos a sus comentarios, saludos cordiales y gracias de antemano,\n\n" +
                        "Julio Cornejo\n" +
                        "Cel. 993003452",
                largoCsv,
                observacion
        );

        helper.setText(cuerpo);

        // Adjuntar el archivo
        helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));

        emailSender.send(message);
    }
}

