import { Component, OnInit, signal } from '@angular/core';
import { Service, ServiceCatalogService } from '../../../core/services/service-catalog.service';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  services = signal<Service[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  constructor(private readonly serviceCatalogService: ServiceCatalogService) { }

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.loading.set(true);
    this.serviceCatalogService.findAll().subscribe({
      next: (data) => {
        this.services.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load services');
        this.loading.set(false);
        console.error(err);
      }
    });
  }
}
