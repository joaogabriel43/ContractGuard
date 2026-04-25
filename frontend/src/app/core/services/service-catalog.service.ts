import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Service {
  id: string;
  name: string;
  slug: string;
  createdAt: string;
}

export interface ViolationResponse {
  path: string;
  httpMethod: string;
  ruleType: string;
  severity: string;
  message: string;
}

export interface DiffReportResponse {
  id: string;
  serviceId: string;
  baseSpecVersion: string | null;
  candidateSpecVersion: string;
  hasBreakingChanges: boolean;
  violationCount: number;
  violations: ViolationResponse[];
  generatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ServiceCatalogService {

  private readonly apiUrl = `${environment.apiUrl}/services`;

  constructor(private readonly http: HttpClient) { }

  findAll(): Observable<Service[]> {
    return this.http.get<Service[]>(this.apiUrl);
  }

  getLatestReport(slug: string): Observable<DiffReportResponse> {
    return this.http.get<DiffReportResponse>(`${this.apiUrl}/${slug}/reports/latest`);
  }
}

