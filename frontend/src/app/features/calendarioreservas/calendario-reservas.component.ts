import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions } from '@fullcalendar/core';
import esLocale from '@fullcalendar/core/locales/es';

import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';

import { ReservaCalendario } from '../../core/models/reservacalendario.model';
import { ReservaService } from '../../core/services/reserva.service';

@Component({
  selector: 'app-calendario-reservas',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FullCalendarModule],
  templateUrl: './calendario-reservas.component.html',
  styleUrls: ['./calendario-reservas.component.scss'],
})
export class CalendarioReservasComponent implements OnInit {
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
  };

  constructor(private reservaService: ReservaService) {}

  ngOnInit(): void {
    const hoy = new Date();
    const mes = hoy.getMonth() + 1;
    const anio = hoy.getFullYear();

    this.reservaService
      .getCalendario(mes, anio)
      .subscribe((data: ReservaCalendario[]) => {
        this.calendarOptions.events = data.map((r) => ({
          title: `${r.sala} - ${r.usuario}`,
          start: r.fecha,
          allDay: true,
          extendedProps: {
            estado: r.estado,
          },
        }));
      });
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
