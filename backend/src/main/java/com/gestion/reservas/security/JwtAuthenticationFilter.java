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

        System.out.println("entra doFilterInternal 1 " + request.getMethod() + " " + request.getRequestURI());


        String authHeader = request.getHeader("Authorization");
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else if (request.getParameter("token") != null) {
            jwt = request.getParameter("token");
        }

        if (jwt == null) {

            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("entra doFilterInternal 2");

        Long idUsuario = jwtService.extractIdUsuario(jwt);

        if (idUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            System.out.println("entra doFilterInternal 3");

            UserDetails userDetails = userDetailsService.loadUserById(idUsuario);

            System.out.println("entra doFilterInternal 4");

            if (jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println("entra doFilterInternal 5");


                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                System.out.println("entra doFilterInternal 6");

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } else {
                System.out.println("Token inválido");
            }
        }
        System.out.println("entra doFilterInternal 7");

        filterChain.doFilter(request, response);
    }
}
