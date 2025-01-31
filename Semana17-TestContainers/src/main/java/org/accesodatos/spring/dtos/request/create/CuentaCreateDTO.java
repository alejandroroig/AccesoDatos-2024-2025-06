package org.accesodatos.spring.dtos.request.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CuentaCreateDTO {
    @NotNull(message = "El saldo es obligatorio")
    @Min(value = 0, message = "El saldo no puede ser negativo")
    private Double saldo;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Pattern(regexp = "Ahorros|Corriente", message = "El tipo de cuenta debe ser 'Ahorros' o 'Corriente'")
    private String tipoCuenta;

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long idUsuario;
}
