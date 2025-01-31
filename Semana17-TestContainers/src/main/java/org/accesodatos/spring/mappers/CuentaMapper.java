package org.accesodatos.spring.mappers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.accesodatos.spring.dtos.request.create.CuentaCreateDTO;
import org.accesodatos.spring.dtos.request.create.UsuarioCreateDTO;
import org.accesodatos.spring.dtos.response.CuentaDTO;
import org.accesodatos.spring.dtos.response.TransaccionDTO;
import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Data
@RequiredArgsConstructor
public class CuentaMapper {

    private final UsuarioMapper usuarioMapper;
    private final TransaccionMapper transaccionMapper;

    public CuentaDTO toDto(Cuenta cuenta) {
        if (cuenta == null) return null;

        CuentaDTO dto = new CuentaDTO();
        dto.setId(cuenta.getId());
        dto.setSaldo(cuenta.getSaldo());
        dto.setTipoCuenta(cuenta.getTipoCuenta());
        dto.setFechaCreacion(cuenta.getFechaCreacion());
        dto.setIdUsuario(cuenta.getUsuario().getId());

        if (cuenta.getTransacciones() != null) {
            List<TransaccionDTO> transaccionesDto = cuenta.getTransacciones().stream()
                    .map(transaccionMapper::toDto)
                    .toList();
            dto.setTransacciones(transaccionesDto);
        }

        return dto;
    }

    public Cuenta toEntity(CuentaCreateDTO dto) {
        if (dto == null) return null;

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(dto.getSaldo());
        cuenta.setTipoCuenta(dto.getTipoCuenta());

        return cuenta;
    }
}
