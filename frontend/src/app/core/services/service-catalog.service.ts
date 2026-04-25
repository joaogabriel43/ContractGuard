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

@Injectable({
  providedIn: 'root'
})
export class ServiceCatalogService {

  private readonly apiUrl = `${environment.apiUrl}/services`;

  constructor(private readonly http: HttpClient) { }

  findAll(): Observable<Service[]> {
    return this.http.get<Service[]>(this.apiUrl);
  }
}
