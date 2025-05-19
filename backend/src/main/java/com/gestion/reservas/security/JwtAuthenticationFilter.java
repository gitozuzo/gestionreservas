package com.gestion.reservas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println(" Método: " + request.getMethod() + " | URI: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        String jwt = null;

        // Obtener token JWT del header o parámetro
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else if (request.getParameter("token") != null) {
            jwt = request.getParameter("token");
        }

        // Si no hay token, seguir sin autenticar
        if (jwt == null) {
            System.out.println("No se proporcionó token. Continuando como anónimo.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            System.out.println(" Token recibido. Intentando autenticar...");


            Long idUsuario = jwtService.extractIdUsuario(jwt);

            if (idUsuario == null) {
                System.out.println("No se pudo extraer el ID del usuario desde el token.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado.");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                System.out.println("Usuario ya autenticado en el contexto.");
                filterChain.doFilter(request, response);
                return;
            }

            // Cargar usuario desde DB
            UserDetails userDetails = userDetailsService.loadUserById(idUsuario);
            System.out.println("Usuario cargado: " + userDetails.getUsername());
            System.out.println("Roles: " + userDetails.getAuthorities());

            // Validar el token contra los datos del usuario
            if (!jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println("Token no válido para este usuario.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido.");
                return;
            }

            // Crear el token de autenticación de Spring
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("Autenticación exitosa.");

        } catch (Exception e) {
            System.out.println("Error durante la autenticación: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error de autenticación: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);

    }
}
