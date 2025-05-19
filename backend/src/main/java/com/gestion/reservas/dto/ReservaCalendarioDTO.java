package com.gestion.reservas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaCalendarioDTO {
    private Long idReserva;
    private LocalDate fecha;
    private String sala;
    private String usuario;
    private String estado;
    private String color;
}
