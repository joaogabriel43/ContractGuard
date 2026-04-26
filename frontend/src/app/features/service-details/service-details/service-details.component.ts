import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { DiffReportResponse, ServiceCatalogService } from '../../../core/services/service-catalog.service';

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './service-details.component.html',
  styleUrl: './service-details.component.scss'
})
export class ServiceDetailsComponent implements OnInit {

  slug = signal<string | null>(null);
  report = signal<DiffReportResponse | null>(null);
  loadingReport = signal<boolean>(true);
  reportError = signal<string | null>(null);

  selectedFile = signal<File | null>(null);
  fileContent = signal<string>('');
  version = signal<string>('');
  isDragOver = signal<boolean>(false);
  analyzing = signal<boolean>(false);
  analyzeError = signal<string | null>(null);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly serviceCatalogService: ServiceCatalogService
  ) {}

  ngOnInit(): void {
    const paramSlug = this.route.snapshot.paramMap.get('slug');
    if (paramSlug) {
      this.slug.set(paramSlug);
      this.loadLatestReport(paramSlug);
    } else {
      this.loadingReport.set(false);
    }
  }

  loadLatestReport(slug: string): void {
    this.loadingReport.set(true);
    this.reportError.set(null);
    this.serviceCatalogService.getLatestReport(slug).subscribe({
      next: (data) => {
        this.report.set(data);
        this.loadingReport.set(false);
      },
      error: (err) => {
        if (err.status !== 404) {
          this.reportError.set('Failed to load latest report.');
        }
        this.loadingReport.set(false);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) this.readFile(input.files[0]);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver.set(true);
  }

  onDragLeave(): void {
    this.isDragOver.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.readFile(file);
  }

  private readFile(file: File): void {
    this.selectedFile.set(file);
    const reader = new FileReader();
    reader.onload = (e) => this.fileContent.set(e.target?.result as string);
    reader.readAsText(file);
  }

  canAnalyze(): boolean {
    return !!this.fileContent() && this.version().trim().length > 0 && !this.analyzing();
  }

  analyze(): void {
    const slug = this.slug();
    if (!slug || !this.canAnalyze()) return;

    this.analyzing.set(true);
    this.analyzeError.set(null);

    this.serviceCatalogService.analyzeContract(slug, this.version().trim(), this.fileContent()).subscribe({
      next: (report) => {
        this.report.set(report);
        this.analyzing.set(false);
        this.selectedFile.set(null);
        this.fileContent.set('');
        this.version.set('');
      },
      error: (err) => {
        this.analyzeError.set(err.error?.message ?? 'Analysis failed. Check the file format and try again.');
        this.analyzing.set(false);
      }
    });
  }

  getSeverityClasses(severity: string): string {
    switch (severity) {
      case 'BREAKING': return 'bg-apple-red/15 text-apple-red border-apple-red/30';
      case 'WARNING':  return 'bg-apple-yellow/15 text-apple-yellow border-apple-yellow/30';
      default:         return 'bg-apple-blue/15 text-apple-blue border-apple-blue/30';
    }
  }
}
