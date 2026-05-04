package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.model.exceptions.ImsbException;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.ItemValue;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.LoginResponse;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.*;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginService {

        @Autowired
        private final UsuariosRepository usuariosRepository;

        @Autowired
        private final MenuRepository menuRepository;

        @Autowired
        private final MenuRolRepository menuRolRepository;

        @Autowired
        private final UnidadRepository unidadRepository;

        @Autowired
        private final EmpresaClienteRepository empresaClienteRepository;

    @Transactional("usuariosTransactionManager")
    public LoginResponse login(String username, String password, String codEmpresa) {

        List<ItemValue> items = Arrays.asList();

        Usuarios usuario = usuariosRepository
                .findByUsernameAndPassword(username, password)
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        Unidad unidadObj = unidadRepository.findById(usuario.getIdUnidad().getIdUnidad())
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        EmpresaCliente empresaCliente = empresaClienteRepository.findById(unidadObj.getEmpresaCliente().getIdEmpresaCliente())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // Validar si el Usuario esta habilitado para la Empresa x
        if (!empresaCliente.getCodigoEmpresaCliente().equals(codEmpresa)) {
            throw new ImsbException("No se pudo obtener usuario en empresa " + codEmpresa, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Set<String> codMenus = menuRolRepository.findByRole(usuario.getIdRole()).stream()
                .map(MenuRol::getMenu)
                .filter(menu -> menu != null && Boolean.TRUE.equals(menu.getEstadoMenu()))
                .map(Menu::getCodMenu)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String urlEmpresa = empresaCliente.getUrlEmpresaCliente();
        String nombreUnidadLower = unidadObj.getShowNombreUnidad().toLowerCase();

        if(empresaCliente.getCodigoEmpresaCliente().equalsIgnoreCase(codEmpresa)){
            final String finalUnidadFiltro = getUnidad(unidadObj.getShowNombreUnidad().toLowerCase());

        items = menuRepository.findAllById(codMenus)
                .stream()
                .filter(menu -> {
                    String menuUrl = menu.getUrl();
                    if (urlEmpresa == null || urlEmpresa.isEmpty() || menuUrl == null) return false;

                    // 1. Siempre debe contener la URL de la empresa
                    if (!menuUrl.contains(urlEmpresa)) return false;

                    // 2. Lógica especial para Imprenta (según tu requerimiento de cargar /imsb/cobranza/registro)
                    if (nombreUnidadLower.contains("imprenta")) {
                        return menuUrl.contains("imprenta");
                    }

                    // 3. Lógica para Tesorería (Cobranza) y Juzgado (Notificación)
                    if (!finalUnidadFiltro.isEmpty()) {
                        return menuUrl.contains(finalUnidadFiltro);
                    }
                    return true;
                })
                .map(menu -> {
                    ItemValue item = new ItemValue();
                    item.setId(menu.getCodMenu());
                    item.setValue1(menu.getNombre());
                    item.setValue2(menu.getUrl());
                    item.setOrden(menu.getOrden());
                    return item;
                })
                .sorted(Comparator.comparingInt(ItemValue::getOrden))
                .toList();
        }
        return LoginResponse.builder()
                .username(usuario.getUsername())
                .nombreMember(usuario.getNombreMember())
                .apellidoPaternoMember(usuario.getApellidoPaternoMember())
                .apellidoMaternoMember(usuario.getRut())
                .telefonoContactoMember(usuario.getTelefonoContactoMember())
                .emailPerfil(usuario.getEmailPerfil())
                .unidad(unidadObj)
                .empresa(empresaCliente)
                .role(usuario.getIdRole())
                .items(items)
                .build();
    }

    public String getUnidad(String nombreUnidadLower){
        // --- LÓGICA DE ASIGNACIÓN DE UNIDAD (MAPEO) ---
        String unidadFiltro = "";
        if (nombreUnidadLower.contains("tesoreria")) {
            unidadFiltro = "cobranza";
        } else if (nombreUnidadLower.contains("juzgado")) {
            unidadFiltro = "notificacion";
        } else if (nombreUnidadLower.contains("imprenta")) {
            unidadFiltro = "imprenta"; // O el valor que corresponda a tus URLs de imprenta
        }

        return unidadFiltro;
    }
}
