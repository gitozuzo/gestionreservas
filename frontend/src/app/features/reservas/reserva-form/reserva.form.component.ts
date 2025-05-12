import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { AuthStore } from '../../../core/auth/auth.store';
import { Recomendacion } from '../../../core/models/recomendacion.model';
import { Reserva } from '../../../core/models/reserva.model';
import { EspacioService } from '../../../core/services/espacio.service';
import { EstadoReservaService } from '../../../core/services/estadoreserva.service';
import { RecomendacionService } from '../../../core/services/recomendacion.service';
import { ReservaService } from '../../../core/services/reserva.service';
import { UsuarioService } from '../../../core/services/usuario.service';

@Component({
  selector: 'app-reserva-form',
  templateUrl: './reserva.form.component.html',
  styleUrls: ['./reserva.form.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
})
export class ReservaFormComponent implements OnInit {
  form: FormGroup;
  modoEdicion = false;
  idReserva?: number;

  usuarios: any[] = [];
  espacios: any[] = [];
  estados: any[] = [];
  recomendaciones: Recomendacion[] = [];
  mostrarRecomendaciones = false;
  cargandoRecomendaciones = false;

  nombreDiaSemana: string = '';
  espacioSeleccionado: any = null;
  usuarioNombre: string = '';

  constructor(
    private fb: FormBuilder,
    private reservaService: ReservaService,
    private recomendacionService: RecomendacionService,
    private usuarioService: UsuarioService,
    private espacioService: EspacioService,
    private estadoReservaService: EstadoReservaService,
    private authStore: AuthStore,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      idUsuario: ['', Validators.required],
      idEspacio: ['', Validators.required],
      idEstado: ['', Validators.required],
      fechaInicio: ['', Validators.required],
      horaInicio: ['', Validators.required],
      fechaFin: ['', Validators.required],
      horaFin: ['', Validators.required],
      numeroOcupantes: ['', [Validators.required, Validators.min(1)]],
      sincronizado: [false],
    });
  }

  ngOnInit(): void {
    const usuarioId = this.authStore.getIdUsuarioValue();
    if (usuarioId) {
      this.form.patchValue({ idUsuario: usuarioId });
    }

    this.usuarioService.getUsuarios().subscribe((data) => {
      this.usuarios = data;
      this.usuarioNombre =
        this.usuarios.find((u) => u.idUsuario === usuarioId)?.nombre || '';
    });

    this.espacioService.getEspacios().subscribe((data) => {
      this.espacios = data;
      this.actualizarEspacioSeleccionado();
    });

    this.estadoReservaService.getEstadosReserva().subscribe((data) => {
      this.estados = data;
    });

    this.form.get('idEspacio')?.valueChanges.subscribe(() => {
      this.actualizarEspacioSeleccionado();
    });

    this.route.params.subscribe((params) => {
      if (params['id']) {
        this.modoEdicion = true;
        this.idReserva = +params['id'];
        this.reservaService
          .getReservaById(this.idReserva)
          .subscribe((reserva) => {
            this.form.patchValue({
              ...reserva,
              horaInicio: reserva.fechaInicio.slice(11, 16),
              horaFin: reserva.fechaFin.slice(11, 16),
              fechaInicio: reserva.fechaInicio.slice(0, 10),
              fechaFin: reserva.fechaFin.slice(0, 10),
              numeroOcupantes: reserva.ocupantes,
            });
            this.actualizarEspacioSeleccionado();
          });
      } else {
        this.obtenerRecomendacionesAlta();
      }
    });
  }

  actualizarEspacioSeleccionado(): void {
    const id = +this.form.get('idEspacio')?.value;
    this.espacioSeleccionado =
      this.espacios.find((e) => e.idEspacio === id) || null;
  }

  obtenerRecomendacionesAlta(): void {
    const usuarioId = this.authStore.getIdUsuarioValue();
    if (!usuarioId) return;

    const fecha = new Date();
    const diaSemana = fecha.getDay();
    const hora = -1;

    const dias = [
      'domingo',
      'lunes',
      'martes',
      'miércoles',
      'jueves',
      'viernes',
      'sábado',
    ];
    this.nombreDiaSemana = dias[diaSemana];

    this.cargandoRecomendaciones = true;
    this.recomendacionService
      .getRecomendaciones(usuarioId, diaSemana, hora)
      .subscribe({
        next: (res) => {
          this.recomendaciones = res.recomendaciones;
          this.mostrarRecomendaciones = this.recomendaciones.length > 0;
          this.cargandoRecomendaciones = false;
        },
        error: () => {
          this.cargandoRecomendaciones = false;
          this.recomendaciones = [];
          this.mostrarRecomendaciones = false;
        },
      });
  }

  aplicarRecomendacion(rec: Recomendacion): void {
    this.form.patchValue({
      idEspacio: rec.espacio_id,
    });
    this.espacioSeleccionado =
      this.espacios.find((e) => e.idEspacio === rec.espacio_id) || null;
  }

  guardar(): void {
    if (this.form.invalid) return;

    const formValue = this.form.value;

    const reserva: Reserva = {
      idReserva: this.idReserva || 0,
      idUsuario: formValue.idUsuario,
      idEspacio: formValue.idEspacio,
      idEstado: formValue.idEstado,
      fechaInicio: this.combinarFechaYHora(
        formValue.fechaInicio,
        formValue.horaInicio
      ),
      fechaFin: this.combinarFechaYHora(formValue.fechaFin, formValue.horaFin),
      sincronizado: formValue.sincronizado,
      ocupantes: formValue.numeroOcupantes,
      nombreUsuario: '',
      telefonoUsuario: '',
      nombreEspacio: '',
      imagenEspacio: '',
      descripcionEstado: '',
    };

    const obs =
      this.modoEdicion && this.idReserva
        ? this.reservaService.updateReserva(this.idReserva, reserva)
        : this.reservaService.createReserva(reserva);

    obs.subscribe(() => this.router.navigate(['/reservas']));
  }

  private combinarFechaYHora(fecha: string, hora: string): string {
    return `${fecha}T${hora}`;
  }

  getImagenUrl(nombreImagen: string): string {
    return `${environment.apiUrl}/uploads/${nombreImagen}`;
  }
}
