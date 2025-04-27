import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';
import { UserRole } from '../../core/auth/user-rol.enum';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent {
  UserRol = UserRole;

  constructor(public auth: AuthStore, private router: Router) {
    console.log(this.auth.getNameValue());
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
