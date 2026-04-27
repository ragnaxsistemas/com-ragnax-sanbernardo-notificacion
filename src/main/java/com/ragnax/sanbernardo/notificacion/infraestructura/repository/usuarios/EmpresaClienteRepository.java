package com.ragnax.sanbernardo.notificacion.infraestructura.repository.usuarios;

import com.ragnax.sanbernardo.notificacion.infraestructura.entity.usuarios.EmpresaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaClienteRepository extends JpaRepository<EmpresaCliente, Integer> {

    Optional<EmpresaCliente> findByCodigoEmpresaCliente(String codigoEmpresaCliente);

}
