package org.accesodatos.spring.mappers;

import lombok.Data;
import org.accesodatos.spring.dtos.request.create.PerfilCreateDTO;
import org.accesodatos.spring.dtos.request.patch.PerfilPatchDTO;
import org.accesodatos.spring.dtos.request.update.PerfilUpdateDTO;
import org.accesodatos.spring.dtos.response.PerfilDTO;
import org.accesodatos.spring.models.Perfil;
import org.springframework.stereotype.Component;

@Component
@Data
public class PerfilMapper {

    public PerfilDTO toDto(Perfil perfil) {
        if (perfil == null) return null;

        PerfilDTO dto = new PerfilDTO();
        dto.setId(perfil.getId());
        dto.setNombreCompleto(perfil.getNombreCompleto());
        dto.setTelefono(perfil.getTelefono());
        dto.setDireccion(perfil.getDireccion());
        return dto;
    }

    public Perfil toEntity(PerfilCreateDTO dto) {
        if (dto == null) return null;

        Perfil perfil = new Perfil();
        perfil.setNombreCompleto(dto.getNombreCompleto());
        perfil.setTelefono(dto.getTelefono());
        perfil.setDireccion(dto.getDireccion());
        return perfil;
    }

    public void updateEntityFromDto(PerfilUpdateDTO dto, Perfil perfil) {
        if (dto == null || perfil == null) return;

        perfil.setTelefono(dto.getTelefono());
        perfil.setDireccion(dto.getDireccion());
    }

    public void updateEntityFromPatchDto(PerfilPatchDTO dto, Perfil perfil) {
        if (dto.getTelefono() != null) {
            perfil.setTelefono(dto.getTelefono());
        }
        if (dto.getDireccion() != null) {
            perfil.setDireccion(dto.getDireccion());
        }
    }
}