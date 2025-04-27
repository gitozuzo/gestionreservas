import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EstadoUsuario } from '../../../core/models/estadousuario.model';
import { Rol } from '../../../core/models/rol.model';
import { UsuarioRequestDTO } from '../../../core/models/usuario-request.dto';
import { EstadoUsuarioService } from '../../../core/services/estadousuario.service';
import { RolService } from '../../../core/services/rol.service';
import { UsuarioService } from '../../../core/services/usuario.service';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuario-form.component.html',
})
export class UsuarioFormComponent implements OnInit {
  form!: FormGroup;
  usuarioId?: number;
  isEditMode = false;
  roles: Rol[] = [];
  estadosUsuario: EstadoUsuario[] = [];

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private estadoUsuarioService: EstadoUsuarioService,
    private rolService: RolService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuarioId = Number(this.route.snapshot.paramMap.get('id'));
    this.isEditMode = !!this.usuarioId;

    this.rolService.getRoles().subscribe((data) => {
      console.log('roles', data);
      this.roles = data;
    });

    this.estadoUsuarioService.getEstadosUsuario().subscribe((data) => {
      console.log('estadousuario', data);
      this.estadosUsuario = data;
    });

    this.initForm();

    if (this.isEditMode) {
      this.usuarioService
        .getUsuarioById(this.usuarioId!)
        .subscribe((usuario) => {
          this.form.patchValue(usuario);
          this.form.get('password')?.clearValidators();
          this.form.get('password')?.updateValueAndValidity();
        });
    }
  }

  initForm() {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: [''],
      direccion: [''],
      idRol: [null, Validators.required],
      idEstado: [null, Validators.required],
      password: ['', this.isEditMode ? [] : [Validators.required]],
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    const usuario: UsuarioRequestDTO = this.form.value;

    console.log(usuario);

    if (this.isEditMode) {
      this.usuarioService
        .updateUsuario(this.usuarioId!, usuario)
        .subscribe(() => {
          this.router.navigate(['/usuarios']);
        });
    } else {
      console.log('crear usuario', usuario);
      this.usuarioService.createUsuario(usuario).subscribe(() => {
        this.router.navigate(['/usuarios']);
      });
    }
  }

  cancelar() {
    this.router.navigate(['/usuarios']);
  }
}
