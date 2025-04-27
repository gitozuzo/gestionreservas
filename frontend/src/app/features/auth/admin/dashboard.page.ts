import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  templateUrl: './dashboard.page.html',
  styleUrls: ['./dashboard.page.scss'],
  imports: [CommonModule],
})
export class DashboardPage implements OnInit {
  stats = {
    usuarios: 5,
    reservas: 12,
    activos: 4,
    inactivos: 1,
  };

  ngOnInit(): void {
    // Aquí podrías llamar a un servicio real en el futuro
  }
}
