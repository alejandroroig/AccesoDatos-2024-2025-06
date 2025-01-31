package org.accesodatos.spring.repositories;

import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    @Query("SELECT c FROM Cuenta c WHERE c.usuario.id = :usuarioId")
    List<Cuenta> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
