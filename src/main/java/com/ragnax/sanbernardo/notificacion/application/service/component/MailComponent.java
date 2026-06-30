package com.ragnax.sanbernardo.notificacion.application.service.component;

import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Unidad;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.Usuarios;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios.UnidadRepository;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios.UsuariosRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class MailComponent {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private UnidadRepository unidadRepository;

    @Autowired
    private ApiProperties apiProperties;

    public  void enviarCorreoNormalizacion(String observacion, String tipo, String unidad, int largoCsv, byte[] archivoAdjunto, String nombreArchivo) {
        log.info("observacion {} - tipo {} - unidad {} - nombreArchivo {}", observacion, tipo, unidad, nombreArchivo);
        try {
            Unidad setAdministracion =  Unidad.builder().idUnidad(1).build();

            MimeMessage message = emailSender.createMimeMessage();

            Optional<Unidad> optUnidad = unidadRepository.findByCodigoUnidad("imsb_".concat(unidad));

            List<Usuarios> lista = usuariosRepository.findByIdUnidad(optUnidad.get());
            String[] mailUnidad = lista.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            String[] mailAdm = listaAdministracionCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String subject = String.format("Solicitud normalizacion %s Correos de Chile - I. Municipalidad de San Bernardo", observacion);

            helper.setFrom(apiProperties.getMailUsername(), "Generacion Cartas Ilustre Municipalidad San bernardo");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo (apiProperties.getMailDestinatarioOficial()); //apiProperties.getMailDestinatarioOficial());
            helper.setCc(mailUnidad);
            //log.info("mailUnidad {}", mailUnidad);
            helper.setBcc(apiProperties.getMailUsername());
            helper.setSubject(subject);

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimada Fernanda,\n\n" +
                            "Buen día, favor solicito normalizar archivo adjunto correspondiente a %,d registros del %s de la I. Municipalidad de San Bernardo.\n\n" +
                            "La presente solicitud corresponde a una %s realizada por la unidad %s.\n\n" +
                            "Quedamos atentos a sus comentarios, saludos cordiales y gracias de antemano.\n\n" +
                            "Julio Cornejo\n" +
                            "Cel. 993003452",
                    largoCsv,
                    observacion,
                    tipo,
                    unidad
            );

            helper.setText(cuerpo);

            // Adjuntar el archivo
            helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));// 'true' para HTML (como tus plantillas)

            emailSender.send(message);
            log.info("subject {} - cuerpo {} - FROM {} - TO {} - CC {} - BCC {} ***", subject, cuerpo,apiProperties.getMailUsername(), apiProperties.getMailDestinatarioOficial(), mailUnidad, apiProperties.getMailUsername());
            log.info("Correo enviado con éxito");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public  void enviarCorreoErrorNormalizacion(String observacion, String tipo, String unidad, String nombreArchivo) {
        log.info("observacion {} - tipo {} - unidad {} - nombreArchivo {}", observacion, tipo, unidad, nombreArchivo);
        try {
            Unidad setAdministracion =  Unidad.builder().idUnidad(1).build();

            MimeMessage message = emailSender.createMimeMessage();

            Optional<Unidad> optUnidad = unidadRepository.findByCodigoUnidad("imsb_".concat(unidad));

            List<Usuarios> lista = usuariosRepository.findByIdUnidad(optUnidad.get());
            String[] mailUnidad = lista.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            String[] mailAdm = listaAdministracionCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String subject = String.format("Error Solicitud normalizacion %s Correos de Chile - I. Municipalidad de San Bernardo", observacion);

            helper.setFrom(apiProperties.getMailUsername(), "Generacion Cartas Ilustre Municipalidad San bernardo");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo (mailUnidad); //usuario Oficiales //"julio.ignacio.cornejo.sb@gmail.com"  //mailUnidad
            helper.setCc(mailAdm); //usuario Administracion //"julio.ignacio.cornejo.sb@gmail.com" //mailAdm

            helper.setBcc(apiProperties.getMailUsername());
            helper.setSubject(subject);

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimados,\n\n" +
                            "Se informa que se ha producido un Error en la Generación de la Normalización del documento adjunto nombrado:\n" +
                            "\"%s\".\n\n" +
                            "Se sugiere revisar:\n" +
                            "1. Formato  del archivo (Excel).\n" +
                            "2. Orden de las columnas.\n" +
                            "3. Contenido de la información.\n\n" +
                            "Quedamos atentos al nuevo archivo para la generación automática de la Normalización.\n\n" +

                            "Saludos cordiales.\n\n" +
                            "Julio Cornejo\n" +
                            "Cel. 993003452",
                    nombreArchivo
            );

            helper.setText(cuerpo);

            emailSender.send(message);
            log.info("subject {} - cuerpo {} - FROM {} - TO {} - CC {} - BCC {} ***", subject, cuerpo,apiProperties.getMailUsername(), apiProperties.getMailDestinatarioOficial(), mailUnidad, apiProperties.getMailUsername());
            log.info("Correo enviado con éxito");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public  void enviarCorreoProcesamiento(String observacion, String tipo, String unidad, int largoCsv, String nombreArchivo) {
        log.info("observacion {} - tipo {} - unidad {} - nombreArchivo {}", observacion, tipo, unidad, nombreArchivo);
        try {
            Unidad setAdministracion =  Unidad.builder().idUnidad(1).build();

            MimeMessage message = emailSender.createMimeMessage();

            Optional<Unidad> optUnidad = unidadRepository.findByCodigoUnidad("imsb_".concat(unidad));

            List<Usuarios> lista = usuariosRepository.findByIdUnidad(optUnidad.get());
            String[] mailUnidad = lista.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            String[] mailAdm = listaAdministracionCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String subject = String.format("Generacion Cartas de %s Correos de Chile - I. Municipalidad de San Bernardo", observacion);


            helper.setFrom(apiProperties.getMailUsername(), "Generacion Cartas Ilustre Municipalidad San bernardo");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo (mailUnidad); //usuario Oficiales //mailUnidad
            helper.setCc(mailAdm); //usuario Administracion //mailAdm

            helper.setBcc(apiProperties.getMailUsername());
            helper.setSubject(subject);

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimados,\n\n" +
                            "Buen día, se han procesado las cartas  correspondiente a %,d registros del %s de la I. Municipalidad de San Bernardo.\n\n" +
                            "La presentes cartas a una %s realizada por la unidad %s.\n\n" +
                            "Quedamos atentos a sus comentarios, saludos cordiales y gracias de antemano.\n\n" +
                            "Julio Cornejo\n" +
                            "Cel. 993003452",
                    largoCsv,
                    observacion,
                    tipo,
                    unidad
            );

            helper.setText(cuerpo);

            emailSender.send(message);
            log.info("subject {} - cuerpo {} - FROM {} - TO {} - CC {} - BCC {} ***", subject, cuerpo,apiProperties.getMailUsername(), apiProperties.getMailDestinatarioOficial(), mailUnidad, apiProperties.getMailUsername());
            log.info("Correo enviado con éxito");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public  void enviarCorreoImprenta(String observacion, String tipo, String unidad, int largoCsv, String nombreArchivo) {
        log.info("observacion {} - tipo {} - unidad {} - nombreArchivo {}", observacion, tipo, unidad, nombreArchivo);
        try {
            Unidad setImprenta =  Unidad.builder().idUnidad(1).build();

            List<Usuarios> listaImprenta = usuariosRepository.findByIdUnidad(setImprenta);
            String[] mailImprenta = listaImprenta.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            Optional<Unidad> optUnidad = unidadRepository.findByCodigoUnidad("imsb_".concat(unidad));

            List<Usuarios> lista = usuariosRepository.findByIdUnidad(optUnidad.get());

            String[] mailUnidadCC = lista.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            Unidad setAdministracion =  Unidad.builder().idUnidad(1).build();

            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            String[] mailAdmCC = listaAdministracionCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            String[] mailsCombinados = Stream.concat(Arrays.stream(mailUnidadCC), Arrays.stream(mailAdmCC))
                    .toArray(String[]::new);

            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String subject = String.format("Generacion Cartas de %s Correos de Chile - I. Municipalidad de San Bernardo", observacion);

            helper.setFrom(apiProperties.getMailUsername(), "Generacion Cartas Ilustre Municipalidad San bernardo");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo ("julio.i.cornejo.g@gmail.com"); //mailImprenta
            helper.setCc("julio.i.cornejo.g@gmail.com"); //mailsCombinados
            //log.info("mailUnidad {}", mailUnidad);
            helper.setBcc(apiProperties.getMailUsername());
            helper.setSubject(subject);

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimados,\n\n" +
                            "Buen día, se han procesado las cartas  correspondiente a %,d registros del %s de la I. Municipalidad de San Bernardo.\n\n" +
                            "La presentes cartas a una %s realizada por la unidad %s.\n\n" +
                            "Quedamos atentos a sus comentarios, saludos cordiales y gracias de antemano.\n\n" +
                            "Julio Cornejo\n" +
                            "Cel. 993003452",
                    largoCsv,
                    observacion,
                    tipo,
                    unidad
            );

            helper.setText(cuerpo);

            emailSender.send(message);
            log.info("subject {} - cuerpo {} - FROM {} - TO {} - CC {} - BCC {} ***", subject, cuerpo,apiProperties.getMailUsername(), mailImprenta, mailsCombinados, apiProperties.getMailUsername());
            log.info("Correo enviado con éxito");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

