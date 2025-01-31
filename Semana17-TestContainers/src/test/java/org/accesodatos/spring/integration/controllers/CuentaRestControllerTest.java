package org.accesodatos.spring.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.accesodatos.spring.controllers.CuentaRestController;
import org.accesodatos.spring.dtos.request.create.TransaccionCreateDTO;
import org.accesodatos.spring.dtos.response.TransaccionDTO;
import org.accesodatos.spring.services.CuentaService;
import org.accesodatos.spring.services.TransaccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentaRestController.class)
@ExtendWith(SpringExtension.class) // Para integración con JUnit 5
public class CuentaRestControllerTest {
    @Autowired
    private MockMvc mockMvc; // Para realizar peticiones HTTP

    @MockitoBean
    private CuentaService cuentaService;

    @MockitoBean
    private TransaccionService transaccionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransaccionCreateDTO transaccionCreateDTO;
    private TransaccionDTO transaccionDTO;

    @BeforeEach
    void setUp() {
        // TransaccionCreateDTO para el request
        transaccionCreateDTO = new TransaccionCreateDTO();
        transaccionCreateDTO.setMonto(250.0);
        transaccionCreateDTO.setTipoTransaccion("Deposito");

        // Respuesta simulada
        transaccionDTO = new TransaccionDTO();
        transaccionDTO.setId(100L);
        transaccionDTO.setMonto(250.0);
        transaccionDTO.setTipoTransaccion("Deposito");
    }

    @Test
    void crearTransaccion_Exito() throws Exception {
        // GIVEN
        when(transaccionService.crearTransaccion(eq(1L), any(TransaccionCreateDTO.class)))
                .thenReturn(transaccionDTO);

        String jsonBody = objectMapper.writeValueAsString(transaccionCreateDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/cuentas/{id}/transacciones", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.monto").value(250.0))
                .andExpect(jsonPath("$.tipoTransaccion").value("Deposito"));

        verify(transaccionService).crearTransaccion(eq(1L), any(TransaccionCreateDTO.class));
    }

    @Test
    void crearTransaccion_MontoNegativoInvalido() throws Exception {
        // GIVEN: Monto negativo (invalido)
        transaccionCreateDTO.setMonto(-50.0);
        String jsonBody = objectMapper.writeValueAsString(transaccionCreateDTO);

        // WHEN & THEN: Se espera 400 Bad Request
        mockMvc.perform(post("/api/cuentas/{id}/transacciones", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());

        verify(transaccionService, never()).crearTransaccion(anyLong(), any(TransaccionCreateDTO.class));
    }

    @Test
    void crearTransaccion_TipoTransaccionInvalido() throws Exception {
        // GIVEN: Tipo de transacción inválido
        transaccionCreateDTO.setTipoTransaccion("Transferencia");
        String jsonBody = objectMapper.writeValueAsString(transaccionCreateDTO);

        // WHEN & THEN: Se espera 400 Bad Request
        mockMvc.perform(post("/api/cuentas/{id}/transacciones", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());

        verify(transaccionService, never()).crearTransaccion(anyLong(), any(TransaccionCreateDTO.class));
    }
}
