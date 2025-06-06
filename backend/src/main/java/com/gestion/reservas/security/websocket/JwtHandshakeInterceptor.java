package com.gestion.reservas.security.websocket;

import com.gestion.reservas.security.JwtService;
import com.gestion.reservas.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            String token = httpRequest.getParameter("token");

            if (token != null && jwtService.validateToken(token)) {
                try {
                    Long idUsuario = jwtService.extractIdUsuario(token);
                    attributes.put("idUsuario", idUsuario);
                    return true;
                } catch (Exception ex) {
                    System.err.println("Error al extraer ID de usuario del token: " + ex.getMessage());

                }
            } else {
                System.err.println("Token JWT inválido o ausente en la conexión WebSocket");
            }
        } else {
            System.err.println("Handshake request no es de tipo ServletServerHttpRequest");
        }

        // Rechaza la conexión WebSocket
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {

    }
}
