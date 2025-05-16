package com.gestion.reservas.service;

import com.gestion.reservas.dto.MisReservasDTO;
import com.gestion.reservas.dto.ReservaDTO;
import com.gestion.reservas.entity.Espacio;
import com.gestion.reservas.entity.EstadoReserva;
import com.gestion.reservas.entity.Reserva;
import com.gestion.reservas.entity.Usuario;
import com.gestion.reservas.mapper.MisReservasMapper;
import com.gestion.reservas.repository.EspacioRepository;
import com.gestion.reservas.repository.EstadoReservaRepository;
import com.gestion.reservas.repository.ReservaRepository;
import com.gestion.reservas.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MisReservasServiceImpl implements MisReservasService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspacioRepository espacioRepository;
    private final EstadoReservaRepository estadoReservaRepository;
    private final NotificacionService notificacionService;
    private final MisReservasMapper mapper;

    @Override
    public List<MisReservasDTO> findAll() {
        return reservaRepository.findAllByOrderByIdReservaDesc()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MisReservasDTO> findById(Long id) {
        return reservaRepository.findById(id)
                .map(mapper::toDTO);
    }

    @Override
    public MisReservasDTO save(MisReservasDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuario().getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Espacio espacio = espacioRepository.findById(dto.getEspacio().getIdEspacio())
                .orElseThrow(() -> new IllegalArgumentException("Espacio no encontrado"));

        EstadoReserva estado = estadoReservaRepository.findById(dto.getEstado().getIdEstado())
                .orElseThrow(() -> new IllegalArgumentException("Estado no encontrado"));

        Reserva reserva = mapper.toEntity(dto, usuario, espacio, estado);
        Reserva saved = reservaRepository.save(reserva);

        String mensaje = String.format(
                "Tu reserva #%S en el espacio %s para el día %s ha sido registrada.",
                reserva.getIdReserva(),
                reserva.getEspacio().getNombre(),
                reserva.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        notificacionService.crearYEnviarNotificacion(usuario, mensaje);

        return mapper.toDTO(saved);
    }

    @Override
    public void deleteById(Long id) {
        reservaRepository.deleteById(id);
    }

    public void cancelarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        EstadoReserva estadoCancelado = estadoReservaRepository.findByDescripcionIgnoreCase("Cancelada")
                .orElseThrow(() -> new IllegalStateException("Estado 'Cancelada' no configurado"));

        reserva.setEstado(estadoCancelado);
        reservaRepository.save(reserva);

        Usuario usuario = usuarioRepository.findById(reserva.getUsuario().getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));


        String mensaje = String.format(
                "Se ha cancelado la reserva #%S del espacio  %s el día %s.",
                reserva.getIdReserva(),
                reserva.getEspacio().getNombre(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

        );
        notificacionService.crearYEnviarNotificacion(usuario, mensaje);

    }


    public void confirmarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        EstadoReserva estadoConfirmada = estadoReservaRepository.findByDescripcionIgnoreCase("Confirmada")
                .orElseThrow(() -> new IllegalStateException("Estado 'Confirmada' no encontrado"));

        reserva.setEstado(estadoConfirmada);
        reservaRepository.save(reserva);

        Usuario usuario = usuarioRepository.findById(reserva.getUsuario().getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));


        String mensaje = String.format(
                "Se ha confirmado la reserva #%S del espacio  %s el día %s.",
                reserva.getIdReserva(),
                reserva.getEspacio().getNombre(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

        );
        notificacionService.crearYEnviarNotificacion(usuario, mensaje);

    }

    public boolean estaDisponible(Long idEspacio, String inicioStr, String finStr) {
        // Parsear las fechas
        LocalDateTime inicio = LocalDateTime.parse(inicioStr);
        LocalDateTime fin = LocalDateTime.parse(finStr);

        // Buscar reservas del espacio que se solapen con ese rango
        List<Reserva> reservasSolapadas = reservaRepository.findByEspacioIdAndRangoFechasSolapado(idEspacio, inicio, fin);

        // Si no hay reservas solapadas, está disponible
        return reservasSolapadas.isEmpty();
    }
}
