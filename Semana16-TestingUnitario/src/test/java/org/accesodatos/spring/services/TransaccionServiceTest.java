package org.accesodatos.spring.services;

import org.accesodatos.spring.dtos.request.create.TransaccionCreateDTO;
import org.accesodatos.spring.dtos.response.TransaccionDTO;
import org.accesodatos.spring.mappers.TransaccionMapper;
import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Transaccion;
import org.accesodatos.spring.repositories.CuentaRepository;
import org.accesodatos.spring.repositories.TransaccionRepository;
import org.accesodatos.spring.services.impl.TransaccionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionMapper transaccionMapper;

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    private Cuenta cuenta;
    private TransaccionCreateDTO transaccionCreateDTO;
    private Transaccion transaccion;
    private TransaccionDTO transaccionDTO; // Para devolver en toDto(...)

    @BeforeEach
    void setUp() {
        // 1) Configurar una cuenta con saldo inicial
        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldo(1000.0);
        cuenta.setTipoCuenta("Ahorros");
        cuenta.setFechaCreacion(LocalDateTime.now());

        // 2) DTO para crear una Transaccion (Deposito de 200)
        transaccionCreateDTO = new TransaccionCreateDTO();
        transaccionCreateDTO.setMonto(200.0);
        transaccionCreateDTO.setTipoTransaccion("Deposito");

        // 3) Instancia de Transaccion simulada para la llamada a toEntity(...) del mapper
        transaccion = new Transaccion();
        transaccion.setId(5L);
        transaccion.setMonto(200.0);
        transaccion.setTipoTransaccion("Deposito");
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setCuenta(cuenta);

        // 4) Instancia de TransaccionDTO simulada para la llamada a toDto(...) del mapper
        transaccionDTO = new TransaccionDTO();
        transaccionDTO.setId(5L);
        transaccionDTO.setMonto(200.0);
        transaccionDTO.setTipoTransaccion("Deposito");
        transaccionDTO.setFecha(transaccion.getFecha());
    }

    @Test
    void crearTransaccionConDeposito_Exito() {
        // GIVEN (contexto o precondiciones)
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(transaccionMapper.toEntity(any(TransaccionCreateDTO.class))).thenReturn(transaccion);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccion);
        when(transaccionMapper.toDto(any(Transaccion.class))).thenReturn(transaccionDTO);

        // WHEN (acción principal)
        TransaccionDTO result = transaccionService.crearTransaccion(1L, transaccionCreateDTO);

        // THEN (verificaciones o aserciones)
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Deposito", result.getTipoTransaccion());
        assertEquals(1200.0, cuenta.getSaldo(), 0.0001);
        verify(cuentaRepository).findById(1L);
        verify(transaccionRepository).save(any(Transaccion.class));
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void crearTransaccionConRetiro_Exito() {
        // GIVEN
        transaccionCreateDTO.setTipoTransaccion("Retiro");
        transaccionCreateDTO.setMonto(300.0);
        transaccion.setTipoTransaccion("Retiro");
        transaccion.setMonto(300.0);

        // Ajustamos transaccionDTO para reflejar Retiro con ID 6
        transaccionDTO.setId(6L);
        transaccionDTO.setTipoTransaccion("Retiro");
        transaccionDTO.setMonto(300.0);

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(transaccionMapper.toEntity(any(TransaccionCreateDTO.class))).thenReturn(transaccion);
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> {
            Transaccion t = inv.getArgument(0, Transaccion.class);
            t.setId(6L);
            return t;
        });
        when(transaccionMapper.toDto(any(Transaccion.class))).thenReturn(transaccionDTO);

        // WHEN
        TransaccionDTO result = transaccionService.crearTransaccion(1L, transaccionCreateDTO);

        // THEN
        assertNotNull(result);
        assertEquals(6L, result.getId());
        assertEquals("Retiro", result.getTipoTransaccion());
        assertEquals(700.0, cuenta.getSaldo(), 0.0001);
        verify(transaccionRepository).save(any(Transaccion.class));
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void crearTransaccion_RetiroSaldoInsuficiente() {
        // GIVEN
        // Saldo 100 => Retiro 300
        cuenta.setSaldo(100.0);
        transaccionCreateDTO.setTipoTransaccion("Retiro");
        transaccionCreateDTO.setMonto(300.0);
        transaccion.setTipoTransaccion("Retiro");
        transaccion.setMonto(300.0);

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(transaccionMapper.toEntity(any(TransaccionCreateDTO.class))).thenReturn(transaccion);

        // WHEN & THEN (combinados en una sola línea de aserción)
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.crearTransaccion(1L, transaccionCreateDTO);
        });
        assertEquals("Saldo insuficiente para el retiro", ex.getMessage());

        verify(transaccionRepository, never()).save(any(Transaccion.class));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    void crearTransaccion_CuentaNoEncontrada() {
        // GIVEN
        when(cuentaRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN y THEN
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
            transaccionService.crearTransaccion(1L, transaccionCreateDTO);
        });
        assertEquals("Cuenta con id 1 no encontrada", ex.getMessage());

        verify(transaccionRepository, never()).save(any(Transaccion.class));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    void crearTransaccion_TipoTransaccionNoValido() {
        // GIVEN
        transaccionCreateDTO.setTipoTransaccion("Transferencia");
        transaccion.setTipoTransaccion("Transferencia");

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(transaccionMapper.toEntity(any(TransaccionCreateDTO.class))).thenReturn(transaccion);

        // WHEN y THEN
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.crearTransaccion(1L, transaccionCreateDTO);
        });
        assertEquals("Tipo de transacción no válido", ex.getMessage());
    }
}