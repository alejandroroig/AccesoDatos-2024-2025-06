package org.accesodatos.spring.services.impl;

import lombok.RequiredArgsConstructor;
import org.accesodatos.spring.dtos.request.create.CuentaCreateDTO;
import org.accesodatos.spring.dtos.response.CuentaDTO;
import org.accesodatos.spring.mappers.CuentaMapper;
import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Usuario;
import org.accesodatos.spring.repositories.CuentaRepository;
import org.accesodatos.spring.repositories.UsuarioRepository;
import org.accesodatos.spring.services.CuentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {
    private final CuentaRepository cuentaRepository;
    private final CuentaMapper cuentaMapper;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<CuentaDTO> obtenerTodasLasCuentas() {
        return cuentaRepository.findAll()
                .stream()
                .map(cuentaMapper::toDto)
                .toList();
    }

    @Override
    public CuentaDTO obtenerCuentaPorId(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cuenta con id " + id + " no encontrada"));
        return cuentaMapper.toDto(cuenta);
    }

    @Override
    public List<CuentaDTO> obtenerCuentasPorIdUsuario(Long idUsuario) {
        return cuentaRepository.findByUsuarioId(idUsuario)
                .stream()
                .map(cuentaMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CuentaDTO crearCuenta(CuentaCreateDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new NoSuchElementException("Usuario con id " + dto.getIdUsuario() + " no encontrado"));

        Cuenta cuenta = cuentaMapper.toEntity(dto);
        cuenta.setUsuario(usuario);
        cuenta.setFechaCreacion(LocalDateTime.now());

        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);
        return cuentaMapper.toDto(cuentaGuardada);
    }

    @Override
    @Transactional
    public void eliminarCuenta(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cuenta no encontrada con id: " + id));

        // Las transacciones asociadas se eliminarán automáticamente
        // debido a CascadeType.ALL y orphanRemoval = true
        cuentaRepository.delete(cuenta);

    }
}
