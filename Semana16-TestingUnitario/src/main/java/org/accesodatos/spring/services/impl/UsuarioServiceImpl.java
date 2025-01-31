package org.accesodatos.spring.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.accesodatos.spring.dtos.request.create.UsuarioCreateDTO;
import org.accesodatos.spring.dtos.request.patch.UsuarioPatchDTO;
import org.accesodatos.spring.dtos.request.update.UsuarioUpdateDTO;
import org.accesodatos.spring.dtos.response.UsuarioDTO;
import org.accesodatos.spring.mappers.UsuarioMapper;
import org.accesodatos.spring.models.Perfil;
import org.accesodatos.spring.models.Usuario;
import org.accesodatos.spring.repositories.UsuarioRepository;
import org.accesodatos.spring.services.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toDto)
                .toList();
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario con id " + id + " no encontrado"));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO crearUsuario(UsuarioCreateDTO dto) {
        Usuario usuario = usuarioMapper.toEntity(dto);

        if (dto.getFechaRegistro() == null) {
            usuario.setFechaRegistro(LocalDate.now());
        }

        // Sincronizamos la relación bidireccional
        usuario.getPerfil().setUsuario(usuario);

        // Persistimos el usuario (automáticamente persistirá el perfil debido a la cascada)
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuarioGuardado);
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));

        usuarioMapper.updateEntityFromDto(dto, usuarioExistente);

        // Sincronizamos la relación bidireccional
        usuarioExistente.getPerfil().setUsuario(usuarioExistente);

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

        return usuarioMapper.toDto(usuarioActualizado);
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarUsuarioParcial(Long id, JsonNode patch) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));

        UsuarioPatchDTO usuarioPatchDTO = objectMapper.convertValue(patch, UsuarioPatchDTO.class);

        try {
            objectMapper.readerForUpdating(usuarioPatchDTO).readValue(patch);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al procesar el JSON: " + e.getMessage());
        }

        System.out.println("usuarioPatchDTO = " + usuarioPatchDTO);

        // Validar el DTO PATCH
        Set<ConstraintViolation<UsuarioPatchDTO>> violations = validator.validate(usuarioPatchDTO);
        if (!violations.isEmpty()) {
            Map<String, String> errores = new HashMap<>();
            for (ConstraintViolation<UsuarioPatchDTO> violation : violations) {
                errores.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new ConstraintViolationException(errores.toString(), violations);
        }

        // Actualizar la entidad existente con los cambios del DTO PATCH
        usuarioMapper.updateEntityFromPatchDto(usuarioPatchDTO, usuarioExistente);

        // Sincronizar la relación bidireccional
        usuarioExistente.getPerfil().setUsuario(usuarioExistente);

        // Guardar la entidad actualizada
        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

        return usuarioMapper.toDto(usuarioActualizado);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("El usuario con ID " + id + " no existe."));

        // Verificamos que NO tiene cuentas asociadas antes de eliminarlo (ON DELETE RESTRICT)
        if (!usuario.getCuentas().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el usuario con cuentas asociadas.");
        }

        // El perfil asociado se eliminará automáticamente
        // debido a CascadeType.ALL y orphanRemoval = true
        usuarioRepository.delete(usuario);
    }
}
