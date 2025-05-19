import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions } from '@fullcalendar/core';
import esLocale from '@fullcalendar/core/locales/es';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';

import { EstadoReserva } from '../../core/models/estadoreserva.model';
import { TipoEspacio } from '../../core/models/tipoespacio.model';

import { EstadoReservaService } from '../../core/services/estadoreserva.service';
import { ReservaService } from '../../core/services/reserva.service';
import { TipoEspacioService } from '../../core/services/tipoespacio.service';

@Component({
  selector: 'app-calendario-reservas',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FullCalendarModule, FormsModule],
  templateUrl: './calendario-reservas.component.html',
  styleUrls: ['./calendario-reservas.component.scss'],
})
export class CalendarioReservasComponent implements OnInit {
  // Calendario
  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    locale: esLocale,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay',
    },
    height: 'auto',
    eventDisplay: 'block',
    events: [],
    eventClassNames: this.asignarClaseEvento.bind(this),
    datesSet: (info) => {
      const fecha = new Date(info.start);
      this.cargarReservas(fecha.getMonth() + 1, fecha.getFullYear());
    },
  };

  // Filtros
  filtroEmpleado: string = '';
  filtroTipo: string = '';
  filtroEstado: string = '';

  // Datos para los filtros
  tiposEspacio: TipoEspacio[] = [];
  estadosReserva: EstadoReserva[] = [];

  eventosOriginales: any[] = [];

  constructor(
    private reservaService: ReservaService,
    private tipoEspacioService: TipoEspacioService,
    private estadoReservaService: EstadoReservaService
  ) {}

  ngOnInit(): void {
    const hoy = new Date();
    this.cargarReservas(hoy.getMonth() + 1, hoy.getFullYear());

    this.tipoEspacioService.getTiposEspacio().subscribe((tipos) => {
      this.tiposEspacio = tipos;
    });

    this.estadoReservaService.getEstadosReserva().subscribe((estados) => {
      this.estadosReserva = estados;
    });
  }

  cargarReservas(mes: number, anio: number): void {
    this.reservaService.getCalendario(mes, anio).subscribe((data) => {
      this.eventosOriginales = data.map((r) => ({
        title: `${r.sala} - ${r.usuario}`,
        start: r.fecha,
        allDay: true,
        extendedProps: {
          estado: r.estado,
          sala: r.sala,
          tipo: r.tipo,
          usuario: r.usuario,
        },
      }));
      this.aplicarFiltros();
    });
  }

  aplicarFiltros(): void {
    const nombre = this.filtroEmpleado.toLowerCase();
    const tipo = this.filtroTipo;
    const estado = this.filtroEstado;

    const filtrados = this.eventosOriginales.filter((e) => {
      const usuario = e.extendedProps.usuario?.toLowerCase() || '';
      const sala = e.extendedProps.sala?.toLowerCase() || '';
      const matchNombre =
        !nombre || usuario.includes(nombre) || sala.includes(nombre);
      const matchTipo = !tipo || e.extendedProps.tipo === tipo;
      const matchEstado = !estado || e.extendedProps.estado === estado;
      return matchNombre && matchTipo && matchEstado;
    });

    this.calendarOptions.events = filtrados;
  }

  asignarClaseEvento(arg: any): string {
    const estado = arg.event.extendedProps.estado?.toLowerCase();
    switch (estado) {
      case 'confirmada':
        return 'evento-confirmado';
      case 'pendiente':
        return 'evento-pendiente';
      case 'cancelada':
        return 'evento-cancelado';
      default:
        return 'evento-default';
    }
  }
}
