package org.accesodatos.spring.dtos.request.create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TransaccionCreateDTO {
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 0, message = "El monto no puede ser negativo")
    private Double monto;

    @NotBlank(message = "El tipo de transacción es obligatorio")
    @Pattern(regexp = "Deposito|Retiro", message = "El tipo de transacción debe ser 'Deposito' o 'Retiro'")
    private String tipoTransaccion;
}
