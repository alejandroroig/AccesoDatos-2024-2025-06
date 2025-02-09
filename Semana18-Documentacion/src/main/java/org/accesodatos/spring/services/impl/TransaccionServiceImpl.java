package org.accesodatos.spring.services.impl;

import lombok.RequiredArgsConstructor;
import org.accesodatos.spring.dtos.request.create.TransaccionCreateDTO;
import org.accesodatos.spring.dtos.response.TransaccionDTO;
import org.accesodatos.spring.mappers.TransaccionMapper;
import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Transaccion;
import org.accesodatos.spring.repositories.CuentaRepository;
import org.accesodatos.spring.repositories.TransaccionRepository;
import org.accesodatos.spring.services.TransaccionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;
    private final TransaccionMapper transaccionMapper;

    @Override
    public List<TransaccionDTO> obtenerTransaccionesDeCuenta(Long idCuenta) {
        Cuenta cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new NoSuchElementException("Cuenta con id " + idCuenta + " no encontrada"));

        return cuenta.getTransacciones().stream()
                .map(transaccionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TransaccionDTO crearTransaccion(Long idCuenta, TransaccionCreateDTO dto) {
        Cuenta cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new NoSuchElementException("Cuenta con id " + idCuenta + " no encontrada"));

        Transaccion transaccion = transaccionMapper.toEntity(dto);
        transaccion.setCuenta(cuenta);
        transaccion.setFecha(LocalDateTime.now());

        // Actualizar saldo según tipo de transacción
        if ("Deposito".equalsIgnoreCase(transaccion.getTipoTransaccion())) {
            cuenta.setSaldo(cuenta.getSaldo() + transaccion.getMonto());
        } else if ("Retiro".equalsIgnoreCase(transaccion.getTipoTransaccion())) {
            if (cuenta.getSaldo() < transaccion.getMonto()) {
                throw new IllegalArgumentException("Saldo insuficiente para el retiro");
            }
            cuenta.setSaldo(cuenta.getSaldo() - transaccion.getMonto());
        } else {
            throw new IllegalArgumentException("Tipo de transacción no válido");
        }

        transaccionRepository.save(transaccion);
        cuentaRepository.save(cuenta);

        return transaccionMapper.toDto(transaccion);
    }
}
