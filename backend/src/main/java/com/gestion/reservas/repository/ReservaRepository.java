package com.gestion.reservas.repository;

import com.gestion.reservas.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {




    @Query("SELECT MIN(r.fechaInicio) FROM Reserva r")
    LocalDateTime obtenerFechaInicioMinima();

    @Query("SELECT MAX(r.fechaFin) FROM Reserva r")
    LocalDateTime obtenerFechaFinMaxima();

    @Query("""
    SELECT r FROM Reserva r
    JOIN r.espacio e
    WHERE (:fechaInicio IS NULL OR r.fechaInicio >= :fechaInicio)
      AND (:fechaFin IS NULL OR r.fechaFin <= :fechaFin)
      AND (:tipoEspacioId IS NULL OR e.tipoEspacio.idTipoEspacio = :tipoEspacioId)
      AND (:estadoId IS NULL OR r.estado.idEstado = :estadoId)
""")
    List<Reserva> buscarPorFiltros(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("tipoEspacioId") Long tipoEspacioId,
            @Param("estadoId") Long estadoId
    );

    @Query("""
  SELECT r FROM Reserva r
  WHERE r.espacio.idEspacio = :idEspacio
    AND r.estado.descripcion != 'Cancelada'
    AND (
      (r.fechaInicio <= :fin AND r.fechaFin >= :inicio)
    )
""")
    List<Reserva> findByEspacioIdAndRangoFechasSolapado(
            @Param("idEspacio") Long idEspacio,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    List<Reserva> findAllByOrderByIdReservaDesc();

}
