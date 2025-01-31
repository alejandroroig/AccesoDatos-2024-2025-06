package org.accesodatos.spring.services;

import org.accesodatos.spring.dtos.request.create.CuentaCreateDTO;
import org.accesodatos.spring.dtos.response.CuentaDTO;

import java.util.List;

public interface CuentaService {
    List<CuentaDTO> obtenerTodasLasCuentas();
    CuentaDTO obtenerCuentaPorId(Long id);
    List<CuentaDTO> obtenerCuentasPorIdUsuario(Long idUsuario);
    CuentaDTO crearCuenta(CuentaCreateDTO dto);
    void eliminarCuenta(Long id);
}
