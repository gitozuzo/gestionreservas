import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin.guard';
import { empleadoGuard } from './core/auth/empleado.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/layout.component').then((m) => m.LayoutComponent),
    children: [
      {
        path: 'dashboard',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/auth/admin/dashboard.page').then(
            (m) => m.DashboardPage
          ),
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/usuarios/usuarios.page.component').then(
            (m) => m.UsuariosPageComponent
          ),
      },
      {
        path: 'usuarios/nuevo',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/usuarios/usuario-form/usuario-form.componet').then(
            (m) => m.UsuarioFormComponent
          ),
      },
      {
        path: 'usuarios/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/usuarios/usuario-form/usuario-form.componet').then(
            (m) => m.UsuarioFormComponent
          ),
      },

      {
        path: 'espacios',
        canActivate: [adminGuard],
        loadComponent: () =>
          import(
            './features/espacios/espacio-list/espacios.page.component'
          ).then((m) => m.EspaciosPageComponent),
      },
      {
        path: 'espacios/nuevo',
        canActivate: [adminGuard],
        loadComponent: () =>
          import(
            './features/espacios/espacio-form/espacio.form.component'
          ).then((m) => m.EspacioFormComponent),
      },
      {
        path: 'espacios/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () =>
          import(
            './features/espacios/espacio-form/espacio.form.component'
          ).then((m) => m.EspacioFormComponent),
      },

      {
        path: 'reservas',
        canActivate: [empleadoGuard],
        loadComponent: () =>
          import('./features/auth/reservas/reservas.page').then(
            (m) => m.ReservasPage
          ),
      },
      {
        path: '',
        redirectTo: '/login',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.page').then((m) => m.LoginPage),
  },
];
