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
        // DATOS DE EJEMPLO

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
        assertNotNull(result, "El resultado no debería ser null");
        assertEquals(5L, result.getId(), "El ID de la transacción DTO debería ser 5");
        assertEquals("Deposito", result.getTipoTransaccion(), "El tipo de transacción debería ser 'Deposito'");
        assertEquals(1200.0, cuenta.getSaldo(), 0.0001, "El saldo de la cuenta debería aumentar en 200");

        verify(cuentaRepository).findById(1L);
        verify(transaccionMapper).toEntity(any(TransaccionCreateDTO.class));
        verify(transaccionRepository).save(any(Transaccion.class));
        verify(transaccionMapper).toDto(any(Transaccion.class));
        verify(cuentaRepository).save(cuenta);
        verifyNoMoreInteractions(cuentaRepository, transaccionRepository, transaccionMapper);
    }

    @Test
    void crearTransaccionConRetiro_Exito() {
        // GIVEN
        // Cambiamos el tipo de "Retiro"
        transaccionCreateDTO.setTipoTransaccion("Retiro");
        transaccion.setTipoTransaccion("Retiro");
        transaccionDTO.setTipoTransaccion("Retiro");

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(transaccionMapper.toEntity(any(TransaccionCreateDTO.class))).thenReturn(transaccion);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccion);
        when(transaccionMapper.toDto(any(Transaccion.class))).thenReturn(transaccionDTO);

        // WHEN
        TransaccionDTO result = transaccionService.crearTransaccion(1L, transaccionCreateDTO);

        // THEN
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Retiro", result.getTipoTransaccion());
        // Con retiro de 200, el saldo final = 1000 - 200 = 800
        assertEquals(800.0, cuenta.getSaldo(), 0.0001);

        verify(cuentaRepository).findById(1L);
        verify(transaccionMapper).toEntity(any(TransaccionCreateDTO.class));
        verify(transaccionRepository).save(any(Transaccion.class));
        verify(transaccionMapper).toDto(any(Transaccion.class));
        verify(cuentaRepository).save(cuenta);
        verifyNoMoreInteractions(cuentaRepository, transaccionRepository, transaccionMapper);
    }

    @Test
    void crearTransaccion_RetiroSaldoInsuficiente() {
        // GIVEN
        // Bajamos el saldo de la cuenta a 100 -> 100 - 200 = Saldo Insuficiente
        cuenta.setSaldo(100.0);
        transaccionCreateDTO.setTipoTransaccion("Retiro");
        transaccion.setTipoTransaccion("Retiro");

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(transaccionMapper.toEntity(any(TransaccionCreateDTO.class))).thenReturn(transaccion);

        // WHEN
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.crearTransaccion(1L, transaccionCreateDTO);
        });

        // THEN
        assertEquals("Saldo insuficiente para el retiro", ex.getMessage());

        verify(cuentaRepository).findById(1L);
        verify(transaccionMapper).toEntity(any(TransaccionCreateDTO.class));
        // No se llama a transaccionRepository.save ni a cuentaRepository.save
        verify(transaccionRepository, never()).save(any(Transaccion.class));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
        verifyNoMoreInteractions(cuentaRepository, transaccionRepository, transaccionMapper);
    }

    @Test
    void crearTransaccion_CuentaNoEncontrada() {
        // GIVEN
        when(cuentaRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> transaccionService.crearTransaccion(1L, transaccionCreateDTO),
                "Debe lanzar excepción si la cuenta no existe"
        );

        // THEN
        assertEquals("Cuenta con id 1 no encontrada", ex.getMessage());

        verify(cuentaRepository).findById(1L);
        verify(transaccionRepository, never()).save(any(Transaccion.class));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
        verifyNoMoreInteractions(cuentaRepository, transaccionRepository, transaccionMapper);
    }

    @Test
    void crearTransaccion_TipoTransaccionNoValido() {
        // GIVEN
        transaccionCreateDTO.setTipoTransaccion("Transferencia");
        transaccion.setTipoTransaccion("Transferencia");

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(transaccionMapper.toEntity(any(TransaccionCreateDTO.class))).thenReturn(transaccion);

        // WHEN
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transaccionService.crearTransaccion(1L, transaccionCreateDTO),
                "Debe lanzar excepción si el tipo no es 'Deposito' o 'Retiro'"
        );

        // THEN
        assertEquals("Tipo de transacción no válido", ex.getMessage());

        verify(cuentaRepository).findById(1L);
        verify(transaccionMapper).toEntity(any(TransaccionCreateDTO.class));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
        verify(cuentaRepository, never()).save(any(Cuenta.class));
        verifyNoMoreInteractions(cuentaRepository, transaccionRepository, transaccionMapper);
    }
}