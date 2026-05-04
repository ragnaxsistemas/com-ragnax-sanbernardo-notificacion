package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.UnidadDTO;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.CarpetaHabilitada;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.EmpresaCliente;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.CarpetaHabilitadaRepository;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios.EmpresaClienteRepository;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ECarpetaHabilitadaService {

    private final CarpetaHabilitadaRepository repository;

    private final EmpresaClienteRepository empresaClienteRepository;

    private final UnidadRepository unidadRepository;

    @Transactional
    public CarpetaHabilitada registrarHabilitacion(CarpetaHabilitada carpeta) {
        // Aquí podrías agregar validaciones antes de guardar
        return repository.save(carpeta);
    }

    public List<CarpetaHabilitada> listarTodo() {
        return repository.findAll();
    }

    public List<UnidadDTO> obtenerUnidadesFront(String codEmpresa) {
        EmpresaCliente empresa = empresaClienteRepository.findByCodigoEmpresaCliente(codEmpresa)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        return unidadRepository.findByEmpresaCliente(empresa).stream()
                .filter(u -> {
                    String nombre = u.getShowNombreUnidad().toLowerCase();
                    return nombre.contains("tesoreria") || nombre.contains("juzgado");
                })
                .map(u -> UnidadDTO.builder()
                        .codigoUnidad(u.getCodigoUnidad())
                        .nombreUnidad(u.getNombreUnidad())
                        .showNombreUnidad(u.getShowNombreUnidad())
                        .codEmpresa(empresa.getCodigoEmpresaCliente())
                        .build())
                .toList();
    }
}

