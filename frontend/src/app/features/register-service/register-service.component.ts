import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ServiceCatalogService } from '../../core/services/service-catalog.service';

@Component({
  selector: 'app-register-service',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './register-service.component.html',
  styleUrl: './register-service.component.scss'
})
export class RegisterServiceComponent {

  name = signal<string>('');
  slug = signal<string>('');
  loading = signal<boolean>(false);
  error = signal<string | null>(null);

  constructor(
    private readonly serviceCatalogService: ServiceCatalogService,
    private readonly router: Router
  ) {}

  onNameInput(value: string): void {
    this.name.set(value);
    this.slug.set(
      value.toLowerCase()
        .replace(/[^a-z0-9\s-]/g, '')
        .trim()
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
    );
  }

  isValid(): boolean {
    return this.name().trim().length > 0 && /^[a-z0-9][a-z0-9-]*[a-z0-9]$|^[a-z0-9]$/.test(this.slug());
  }

  submit(): void {
    if (!this.isValid() || this.loading()) return;

    this.loading.set(true);
    this.error.set(null);

    this.serviceCatalogService.registerService(this.name().trim(), this.slug()).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.error.set(err.error?.message ?? 'Failed to create service. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
