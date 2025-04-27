package com.gestion.reservas.service;

import com.gestion.reservas.dto.UsuarioRequestDTO;
import com.gestion.reservas.dto.UsuarioResponseDTO;
import com.gestion.reservas.entity.EstadoUsuario;
import com.gestion.reservas.entity.Rol;
import com.gestion.reservas.entity.Usuario;
import com.gestion.reservas.repository.EstadoUsuarioRepository;
import com.gestion.reservas.repository.RolRepository;
import com.gestion.reservas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EstadoUsuarioRepository estadoUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // 🔁 Conversión de DTO a entidad con contraseña cifrada
    public Usuario toEntity(UsuarioRequestDTO dto,Usuario usuarioExistente) {

        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        EstadoUsuario estado = estadoUsuarioRepository.findById(dto.getIdEstado())
                .orElseThrow(() -> new IllegalArgumentException("Estado no encontrado"));

        Usuario usuario = usuarioExistente != null ? usuarioExistente : new Usuario();

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDireccion(dto.getDireccion());
        usuario.setRol(rol);
        usuario.setEstado(estado);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return usuario;

    }

    // 🔁 Conversión de entidad a DTO
    public UsuarioResponseDTO toDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .direccion(usuario.getDireccion())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .idRol(usuario.getRol().getIdRol())
                .rol(usuario.getRol().getDescripcion())
                        .idEstado(usuario.getEstado().getIdEstado())
                .estado(usuario.getEstado().getDescripcion())
                .build();
    }

    // 🔁 Método para listar todos los usuarios como DTOs
    public List<UsuarioResponseDTO> getAllUsuariosDTO() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
