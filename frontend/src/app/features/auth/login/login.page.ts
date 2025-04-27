import { CommonModule, NgClass } from '@angular/common'; // ✅ Añadido NgClass
import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { UserRole } from '../../../core/auth/user-rol.enum';

@Component({
  standalone: true,
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  imports: [CommonModule, ReactiveFormsModule, NgClass],
})
export class LoginPage {
  form: FormGroup;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private authStore: AuthStore,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  onSubmit() {
    if (this.form.valid) {
      const { email, password } = this.form.value;

      this.authService.login(email, password).subscribe({
        next: (response) => {
          this.authStore.login(response.token); // Aquí sí manejas el token
          const rol = this.authStore.getRoleValue();
          console.log(rol);
          this.router.navigate([
            rol === UserRole.ADMIN ? '/dashboard' : '/reservas',
          ]);
        },
        error: (err) => {
          this.errorMessage = err.message;
        },
      });
    }
  }
}
