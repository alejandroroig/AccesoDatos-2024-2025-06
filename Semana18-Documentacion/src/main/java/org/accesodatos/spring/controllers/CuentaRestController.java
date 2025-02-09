package org.accesodatos.spring.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.accesodatos.spring.dtos.request.create.CuentaCreateDTO;
import org.accesodatos.spring.dtos.request.create.TransaccionCreateDTO;
import org.accesodatos.spring.dtos.request.create.UsuarioCreateDTO;
import org.accesodatos.spring.dtos.response.CuentaDTO;
import org.accesodatos.spring.dtos.response.TransaccionDTO;
import org.accesodatos.spring.dtos.response.UsuarioDTO;
import org.accesodatos.spring.services.CuentaService;
import org.accesodatos.spring.services.TransaccionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaRestController {
    private final CuentaService cuentaService;
    private final TransaccionService transaccionService;

    @GetMapping
    @Operation(summary = "Obtiene todas las cuentas")
    public ResponseEntity<List<CuentaDTO>> obtenerTodasLasCuentas() {
        List<CuentaDTO> cuentas = cuentaService.obtenerTodasLasCuentas();
        if (cuentas.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(cuentas); // 200 OK
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una cuenta por su ID")
    public ResponseEntity<CuentaDTO> obtenerCuentaPorId(@PathVariable Long id) {
        CuentaDTO cuentaDTO = cuentaService.obtenerCuentaPorId(id);
        return ResponseEntity.ok(cuentaDTO); // 200 OK
    }

    @GetMapping("/usuario/{id}")
    @Operation(summary = "Obtiene las cuentas de un usuario por su ID")
    public ResponseEntity<List<CuentaDTO>> obtenerCuentasPorUsuarioId(@PathVariable Long id) {
        List<CuentaDTO> cuentas = cuentaService.obtenerCuentasPorIdUsuario(id);
        if (cuentas.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(cuentas); // 200 OK
    }

    @PostMapping
    @Operation(summary = "Crea una nueva cuenta")
    public ResponseEntity<CuentaDTO> crearCuenta(@Valid @RequestBody CuentaCreateDTO dto) {
        CuentaDTO cuentaCreada = cuentaService.crearCuenta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaCreada);
    }

    @PostMapping("/{id}/transacciones")
    @Operation(summary = "Crea una nueva transacción para una cuenta")
    public ResponseEntity<TransaccionDTO> crearTransaccion(@PathVariable Long id, @Valid @RequestBody TransaccionCreateDTO dto) {
        TransaccionDTO transaccionCreada = transaccionService.crearTransaccion(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionCreada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una cuenta por su ID")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id) {
        cuentaService.eliminarCuenta(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
