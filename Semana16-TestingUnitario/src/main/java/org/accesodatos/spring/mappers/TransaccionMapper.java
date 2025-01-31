package org.accesodatos.spring.mappers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.accesodatos.spring.dtos.request.create.TransaccionCreateDTO;
import org.accesodatos.spring.dtos.response.TransaccionDTO;
import org.accesodatos.spring.models.Transaccion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
@RequiredArgsConstructor
public class TransaccionMapper {
    public TransaccionDTO toDto(Transaccion transaccion) {
        if (transaccion == null) return null;

        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(transaccion.getId());
        dto.setMonto(transaccion.getMonto());
        dto.setFecha(transaccion.getFecha());
        dto.setTipoTransaccion(transaccion.getTipoTransaccion());

        return dto;
    }

    public Transaccion toEntity(TransaccionCreateDTO dto) {
        if (dto == null) return null;

        Transaccion transaccion = new Transaccion();

        transaccion.setMonto(dto.getMonto());
        transaccion.setTipoTransaccion(dto.getTipoTransaccion());

        return transaccion;
    }
}
