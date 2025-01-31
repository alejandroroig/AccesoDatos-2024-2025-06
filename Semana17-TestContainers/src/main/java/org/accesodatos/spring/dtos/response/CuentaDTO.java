package org.accesodatos.spring.dtos.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CuentaDTO {
    private Long id;
    private Double saldo;
    private LocalDateTime fechaCreacion;
    private String tipoCuenta;
    private Long idUsuario;
    private List<TransaccionDTO> transacciones;
}
