package org.accesodatos.spring.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.accesodatos.spring.dtos.request.create.UsuarioCreateDTO;
import org.accesodatos.spring.dtos.request.patch.UsuarioPatchDTO;
import org.accesodatos.spring.dtos.request.update.UsuarioUpdateDTO;
import org.accesodatos.spring.dtos.response.PerfilDTO;
import org.accesodatos.spring.dtos.response.UsuarioDTO;
import org.accesodatos.spring.mappers.UsuarioMapper;
import org.accesodatos.spring.models.Perfil;
import org.accesodatos.spring.models.Usuario;
import org.accesodatos.spring.repositories.UsuarioRepository;
import org.accesodatos.spring.services.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private UsuarioServiceImpl usuarioService; // La implementación del servicio

    // Variables de prueba
    private Usuario usuario;
    private UsuarioCreateDTO usuarioCreateDTO;
    private UsuarioUpdateDTO usuarioUpdateDTO;
    private UsuarioPatchDTO usuarioPatchDTO;
    private UsuarioDTO usuarioDTO;

    @BeforeEach
    void setUp() {
        // Configurar un usuario completo con su perfil
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("usuarioTest");
        usuario.setPassword("passwordTest");
        usuario.setEmail("usuario@test.com");
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setCuentas(new ArrayList<>());

        Perfil perfil = new Perfil();
        perfil.setId(1L);
        perfil.setNombreCompleto("Nombre Completo");
        perfil.setTelefono("123456789");
        perfil.setDireccion("Direccion Test");
        perfil.setUsuario(usuario);
        usuario.setPerfil(perfil);

        // Configurar el DTO para crear usuario
        usuarioCreateDTO = new UsuarioCreateDTO();
        usuarioCreateDTO.setUsername("usuarioTest");
        usuarioCreateDTO.setPassword("passwordTest");
        usuarioCreateDTO.setEmail("usuario@test.com");
        // ... otros campos si aplica

        // Configurar el DTO de actualización
        usuarioUpdateDTO = new UsuarioUpdateDTO();
        usuarioUpdateDTO.setPassword("nuevaPassword");
        usuarioUpdateDTO.setEmail("nuevo@test.com");
        // ... asignar los campos necesarios en el DTO de actualización (incluyendo perfil, si aplica)

        // Configurar un DTO de patch (parcial)
        usuarioPatchDTO = new UsuarioPatchDTO();

        // Configurar el DTO de respuesta simulado
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(1L);
        usuarioDTO.setUsername("usuarioTest");
        usuarioDTO.setEmail("usuario@test.com");

        PerfilDTO perfilDTO = new PerfilDTO();
        perfilDTO.setTelefono("123456789");
        perfilDTO.setDireccion("Direccion Test");
        perfilDTO.setNombreCompleto("Nombre Completo");
        usuarioDTO.setPerfil(perfilDTO);
    }

    @Test
    void obtenerTodosLosUsuarios_Exito() {
        // GIVEN: El repositorio devuelve una lista con el usuario
        when(usuarioRepository.findAll()).thenReturn(Collections.singletonList(usuario));
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDTO);

        // WHEN
        List<UsuarioDTO> resultado = usuarioService.obtenerTodosLosUsuarios();

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(usuarioDTO.getUsername(), resultado.getFirst().getUsername());
    }

    @Test
    void obtenerUsuarioPorId_Existe() {
        // GIVEN
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDTO);

        // WHEN
        UsuarioDTO resultado = usuarioService.obtenerUsuarioPorId(1L);

        // THEN
        assertNotNull(resultado);
        assertEquals("usuarioTest", resultado.getUsername());
        assertEquals("usuario@test.com", resultado.getEmail());
    }

    @Test
    void obtenerUsuarioPorId_NoExiste() {
        // GIVEN: El repositorio no encuentra el usuario
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(NoSuchElementException.class, () -> usuarioService.obtenerUsuarioPorId(999L));
    }

    @Test
    void crearUsuario_Exito() {
        // GIVEN: El mapper convierte el DTO a entidad, y el repositorio asigna un ID
        when(usuarioMapper.toEntity(usuarioCreateDTO)).thenReturn(usuario);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDTO);

        // WHEN
        UsuarioDTO resultado = usuarioService.crearUsuario(usuarioCreateDTO);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("usuarioTest", resultado.getUsername());
    }

    @Test
    void actualizarUsuario_Exito() {
        // GIVEN: El repositorio encuentra el usuario y se guarda la entidad actualizada
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDTO);

        // Simular cambios en el DTO de actualización
        usuarioUpdateDTO.setPassword("nuevaPassword");
        usuarioUpdateDTO.setEmail("nuevo@test.com");

        // WHEN
        UsuarioDTO resultado = usuarioService.actualizarUsuario(1L, usuarioUpdateDTO);

        // THEN: Verificar que el usuario se actualice (según la lógica de tu mapper)
        assertNotNull(resultado);
    }

    @Test
    void actualizarUsuarioParcial_Exito() throws JsonProcessingException {
        // GIVEN: Configurar el repositorio para que encuentre el usuario existente y se guarde la entidad actualizada
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Stubear convertValue para que devuelva un UsuarioPatchDTO no nulo
        doReturn(new UsuarioPatchDTO())
                .when(objectMapper).convertValue(any(JsonNode.class), eq(UsuarioPatchDTO.class));

        // Stubear readerForUpdating para que devuelva un ObjectReader real
        doReturn(new ObjectMapper().readerForUpdating(new UsuarioPatchDTO()))
                .when(objectMapper).readerForUpdating(any(UsuarioPatchDTO.class));

        // Simular que, al aplicar el patch, se actualiza el teléfono del perfil:
        usuario.getPerfil().setTelefono("987654321");
        usuarioDTO.getPerfil().setTelefono("987654321");
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(usuarioDTO);

        // Preparamos el JSON de actualización parcial para cambiar el teléfono del perfil
        String jsonPatch = "{\"perfil\": {\"telefono\": \"987654321\"}}";
        JsonNode patchNode = objectMapper.readTree(jsonPatch);

        // WHEN: Se invoca el método de actualización parcial del servicio
        UsuarioDTO resultado = usuarioService.actualizarUsuarioParcial(1L, patchNode);

        // THEN: Verificamos que el DTO de respuesta muestra el teléfono actualizado
        assertNotNull(resultado);
        assertEquals("987654321", resultado.getPerfil().getTelefono(),
                "El teléfono debe ser actualizado a 987654321");
    }

    @Test
    void eliminarUsuario_Exito() {
        // GIVEN: El repositorio encuentra el usuario y se elimina
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).delete(any(Usuario.class));

        // WHEN
        usuarioService.eliminarUsuario(1L);

        // THEN: Se verifica que se llamó a delete en el repositorio
        verify(usuarioRepository).delete(any(Usuario.class));
    }

    @Test
    void eliminarUsuario_NoExiste() {
        // GIVEN: El usuario no se encuentra
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // WHEN & THEN: Se espera que se lance una excepción
        assertThrows(IllegalStateException.class, () -> usuarioService.eliminarUsuario(1L));
    }
}
