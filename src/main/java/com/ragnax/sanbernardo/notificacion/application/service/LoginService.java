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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginService {

        @Autowired
        private final UsuariosRepository usuariosRepository;

        @Autowired
        private final RoleRepository roleRepository;

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

            Usuarios usuario = usuariosRepository
                    .findByUsernameAndPassword(username, password)
                    .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

            Optional<Unidad> optUnidad = unidadRepository.findById(usuario.getIdUnidad().getIdUnidad());

            Optional<EmpresaCliente> optEmpresaCliente = empresaClienteRepository.findById(optUnidad.get().getEmpresaCliente().getIdEmpresaCliente());
            //Validar si el Usuario esta habilitado para la Empresa x
            if(!optEmpresaCliente.isPresent() || !optEmpresaCliente.get().getCodigoEmpresaCliente().equals(codEmpresa)) {
                throw new ImsbException("No se pudo Obtener usuario en empresa "+ codEmpresa, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Role role = usuario.getIdRole();

            List<MenuRol> listMenuRol = menuRolRepository.findByRole(role);

            Set<String> codMenus = listMenuRol.stream()
                    .map(MenuRol::getMenu)
                    .filter(Objects::nonNull)
                    .filter(menu -> Boolean.TRUE.equals(menu.getEstadoMenu()))
                    .map(Menu::getCodMenu)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Optional<Role> optRole = roleRepository.findById(usuario.getIdRole().getId());





            String urlEmpresa = optEmpresaCliente.get().getUrlEmpresaCliente();
            String unidad = optUnidad.get().getShowNombreUnidad().equalsIgnoreCase("tesoreria") ? "cobranza" : "notificacion";

            List<ItemValue> items = menuRepository.findAllById(codMenus)
                    .stream()
                    // 2. Filtramos: Si la URL del menú CONTIENE la de la empresa, SE QUEDA
                    .filter(menu -> {
                        if (urlEmpresa.isEmpty() || menu.getUrl() == null) return false; // Si no hay patrón, no pasa nada
                        if (unidad.isEmpty() || !menu.getUrl().contains(unidad)) return false; // Si no hay patrón, no pasa nada
                        return menu.getUrl().contains(urlEmpresa);
                    })
                    .map(menu -> {
                        ItemValue item = new ItemValue();
                        item.setId(menu.getCodMenu());
                        item.setValue1(menu.getNombre());
                        item.setValue2(menu.getUrl());
                        item.setOrden(menu.getOrden());
                        return item;
                    })
                    .toList();

            /***String codigoUnidad = usuario.getIdUnidad() != null
             ? usuario.getIdUnidad().getCodigoUnidad()
             : null;***/

            return LoginResponse.builder()
                    .username(usuario.getUsername())
                    .nombreMember(usuario.getNombreMember())
                    .apellidoPaternoMember(usuario.getApellidoPaternoMember())
                    .apellidoMaternoMember(usuario.getRut())
                    .telefonoContactoMember(usuario.getTelefonoContactoMember())
                    .emailPerfil(usuario.getEmailPerfil())
                    .unidad(optUnidad.get())
                    .empresa(optEmpresaCliente.get())
                    .role(optRole.get())
                    .items(items)
                    .build();
        }
    }
