package com.gestion.reservas.service;

import com.gestion.reservas.dto.LoginRequestDTO;
import com.gestion.reservas.dto.LoginResponseDTO;
import com.gestion.reservas.entity.Usuario;
import com.gestion.reservas.repository.UsuarioRepository;
import com.gestion.reservas.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginDTO) {

        System.out.println("entra en servicio login 1 " + loginDTO.getEmail() + " " + loginDTO.getPassword());
        try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        } catch (AuthenticationException e) {
            System.err.println("Error de autenticación: " + e.getMessage());
            // Aquí puedes loggear el error con un logger adecuado (e.g., SLF4j)
            // logger.error("Error de autenticación para el usuario {}", loginDTO.getEmail(), e);
            throw e; // Relanza la excepción para que Spring Security pueda manejarla (e.g., devolver un 401)
        }


        System.out.println("entra en servicio login 2");


        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        System.out.println("entra en servicio login 3");

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);

        return new LoginResponseDTO(token);
    }
}