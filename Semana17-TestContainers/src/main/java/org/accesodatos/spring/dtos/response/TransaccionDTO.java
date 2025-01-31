package org.accesodatos.spring.dtos.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransaccionDTO {
    private Long id;
    private Double monto;
    private LocalDateTime fecha;
    private String tipoTransaccion;
}
