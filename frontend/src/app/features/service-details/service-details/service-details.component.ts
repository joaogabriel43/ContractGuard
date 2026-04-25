import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DiffReportResponse, ServiceCatalogService } from '../../../core/services/service-catalog.service';
import { DatePipe, TitleCasePipe } from '@angular/common';

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [RouterLink, DatePipe, TitleCasePipe],
  templateUrl: './service-details.component.html',
  styleUrl: './service-details.component.scss'
})
export class ServiceDetailsComponent implements OnInit {

  slug = signal<string | null>(null);
  report = signal<DiffReportResponse | null>(null);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly serviceCatalogService: ServiceCatalogService
  ) { }

  ngOnInit(): void {
    const paramSlug = this.route.snapshot.paramMap.get('slug');
    if (paramSlug) {
      this.slug.set(paramSlug);
      this.loadLatestReport(paramSlug);
    } else {
      this.loading.set(false);
      this.error.set('Service slug not found in route');
    }
  }

  loadLatestReport(slug: string): void {
    this.loading.set(true);
    this.serviceCatalogService.getLatestReport(slug).subscribe({
      next: (data) => {
        this.report.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        if (err.status === 404) {
          this.error.set('Nenhum relatório de contrato disponível para este serviço ainda.');
        } else {
          this.error.set('Falha ao buscar relatório de contrato.');
        }
        this.loading.set(false);
        console.error(err);
      }
    });
  }
}
