package org.accesodatos.spring.services;

import org.accesodatos.spring.dtos.request.create.TransaccionCreateDTO;
import org.accesodatos.spring.dtos.response.TransaccionDTO;

import java.util.List;

public interface TransaccionService {
    List<TransaccionDTO> obtenerTransaccionesDeCuenta(Long idCuenta);
    TransaccionDTO crearTransaccion(Long idCuenta, TransaccionCreateDTO dto);
}
