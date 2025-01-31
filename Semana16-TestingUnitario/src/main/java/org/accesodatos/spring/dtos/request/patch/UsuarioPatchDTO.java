package org.accesodatos.spring.dtos.request.patch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UsuarioPatchDTO {
    @Size(min = 8, max = 50, message = "La contraseña debe tener entre 8 y 50 caracteres")
    private String password;

    @Email(message = "El formato del email es inválido")
    private String email;

    @Valid
    private PerfilPatchDTO perfil;
}
