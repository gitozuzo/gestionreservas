package com.gestion.reservas.controller;

import com.gestion.reservas.dto.UsuarioRequestDTO;
import com.gestion.reservas.dto.UsuarioResponseDTO;
import com.gestion.reservas.entity.Usuario;
import com.gestion.reservas.service.UsuarioServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // O el dominio de tu frontend
public class UsuarioController {

    private final UsuarioServiceImpl usuarioService;

    // ✅ Listar todos los usuarios (como DTO)
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.getAllUsuariosDTO());
    }

    // ✅ Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(usuarioService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Crear nuevo usuario con validación y cifrado
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> createUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        System.out.println("crear servicio");
        System.out.println(dto);
        Usuario usuario = usuarioService.toEntity(dto,null);
        Usuario creado = usuarioService.save(usuario);
        return ResponseEntity.ok(usuarioService.toDTO(creado));
    }

    // ✅ Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(@PathVariable Long id,
                                                            @Valid @RequestBody UsuarioRequestDTO dto) {
        return usuarioService.findById(id)
                .map(existing -> {
                    Usuario usuario = usuarioService.toEntity(dto,existing);
                    usuario.setIdUsuario(id); // mantiene el mismo ID
                    Usuario actualizado = usuarioService.save(usuario);

                    return ResponseEntity.ok(usuarioService.toDTO(actualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        if (usuarioService.findById(id).isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

