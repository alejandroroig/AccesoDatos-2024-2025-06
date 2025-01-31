package org.accesodatos.spring.integration.repositories;

import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Perfil;
import org.accesodatos.spring.models.Transaccion;
import org.accesodatos.spring.models.Usuario;
import org.accesodatos.spring.repositories.CuentaRepository;
import org.accesodatos.spring.repositories.TransaccionRepository;
import org.accesodatos.spring.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
public class TransaccionRepositoryTest {

    // Configuración del contenedor PostgreSQL
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16.3");

    // Configuración para que Spring se conecte a este contenedor
    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    // Inyección de repositorios
    @Autowired
    private TransaccionRepository transaccionRepository;
    @Autowired
    private CuentaRepository cuentaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Datos de prueba
    private Usuario usuario;
    private Cuenta cuenta;
    private Transaccion transaccion;

    @BeforeEach
    void setUp() {
        // Creamos un usuario con perfil
        usuario = new Usuario();
        usuario = new Usuario();
        usuario.setUsername("usuarioTest");
        usuario.setPassword("passwordTest");
        usuario.setEmail("usuario@test.com");
        usuario.setFechaRegistro(LocalDate.now());

        Perfil perfil = new Perfil();
        perfil.setNombreCompleto("Nombre Completo");
        perfil.setTelefono("123456789");
        perfil.setDireccion("Direccion Test");

        // Sincronización bidireccional
        perfil.setUsuario(usuario);
        usuario.setPerfil(perfil);

        usuarioRepository.save(usuario);

        // Creamos una cuenta para dicho usuario
        cuenta = new Cuenta();
        cuenta.setSaldo(1000.0);
        cuenta.setFechaCreacion(LocalDateTime.now());
        cuenta.setTipoCuenta("Ahorros");
        cuenta.setUsuario(usuario);

        cuentaRepository.save(cuenta);

        // Creamos una transacción para esa cuenta
        transaccion = new Transaccion();
        transaccion.setMonto(200.0);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setTipoTransaccion("Deposito");
        transaccion.setCuenta(cuenta);
    }

    @Test
    void findByCuentaId_Exito() {
        // GIVEN: Guardamos la transacción
        transaccionRepository.save(transaccion);

        // WHEN: Recuperamos las transacciones para la cuenta
        List<Transaccion> transacciones = transaccionRepository.findByCuentaId(cuenta.getId());

        // THEN: La lista debe contener una transacción con tipo "Deposito"
        assertNotNull(transacciones, "La lista de transacciones no debe ser null");
        assertEquals(1, transacciones.size(), "Debe haber 1 transacción para la cuenta");
        assertTrue(transacciones.stream().anyMatch(t -> "Deposito".equals(t.getTipoTransaccion())));
        assertFalse(transacciones.stream().anyMatch(t -> "Retiro".equals(t.getTipoTransaccion())));
    }

    @Test
    void findByCuentaId_SinTransacciones_DeberiaRetornarListaVacia() {
        // WHEN: Sin guardar ninguna transacción, consultamos por la cuenta
        List<Transaccion> transacciones = transaccionRepository.findByCuentaId(cuenta.getId());
        // THEN: La lista debe estar vacía
        assertTrue(transacciones.isEmpty(), "La lista debe estar vacía si no hay transacciones");
    }

    @Test
    void saveAndRetrieveTransaccion_Exito() {
        // GIVEN: Guardamos la transacción
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        assertNotNull(transaccionGuardada.getId(), "El ID de la transacción no debe ser null");

        // WHEN: Recuperamos la transacción por su ID
        Optional<Transaccion> transaccionRecuperada = transaccionRepository.findById(transaccionGuardada.getId());

        // THEN: Verificamos que la transacción se recupera correctamente
        assertTrue(transaccionRecuperada.isPresent(), "La transacción debe existir");
        assertEquals("Deposito", transaccionRecuperada.get().getTipoTransaccion());
        assertEquals(200.0, transaccionRecuperada.get().getMonto());
    }

    @Test
    void updateTransaccion_Exito() {
        // GIVEN: Guardamos la transacción
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        Long transaccionId = transaccionGuardada.getId();

        // WHEN: Actualizamos el monto a 300 y guardamos nuevamente
        transaccionGuardada.setMonto(300.0);
        transaccionRepository.save(transaccionGuardada);

        // THEN: Recuperamos la transacción y verificamos la actualización
        Optional<Transaccion> transaccionActualizada = transaccionRepository.findById(transaccionId);
        assertTrue(transaccionActualizada.isPresent(), "La transacción actualizada debe existir");
        assertEquals(300.0, transaccionActualizada.get().getMonto(), "El monto actualizado debe ser 300");
    }

    @Test
    void deleteTransaccion_Exito() {
        // GIVEN: Guardamos la transacción
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        Long id = transaccionGuardada.getId();
        assertNotNull(id, "El ID debe ser asignado al guardar la transacción");

        // WHEN: Eliminamos la transacción
        transaccionRepository.deleteById(id);

        // THEN: Verificamos que la transacción ya no existe
        Optional<Transaccion> transaccionRecuperada = transaccionRepository.findById(id);
        assertFalse(transaccionRecuperada.isPresent(), "La transacción debe haber sido eliminada");
    }
}
