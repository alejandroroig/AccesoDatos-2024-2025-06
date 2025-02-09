package org.accesodatos.spring.repositories;

import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    @Query("SELECT t FROM Transaccion t WHERE t.cuenta.id = :cuentaId")
    List<Transaccion> findByCuentaId(@Param("cuentaId") Long cuentaId);
}
