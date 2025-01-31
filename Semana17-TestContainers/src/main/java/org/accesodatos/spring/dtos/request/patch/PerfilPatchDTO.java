package org.accesodatos.spring.dtos.request.patch;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PerfilPatchDTO {
    @Pattern(regexp = "[0-9]*", message = "El teléfono solo puede contener números (sin espacios ni otros carácteres)")
    private String telefono;

    @Size(max = 50, message = "La dirección no puede superar los 50 caracteres")
    private String direccion;
}
