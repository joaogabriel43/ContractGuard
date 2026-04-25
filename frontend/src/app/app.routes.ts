import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard.component';
import { ServiceDetailsComponent } from './features/service-details/service-details/service-details.component';

export const routes: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            {
                path: '',
                component: DashboardComponent,
                title: 'ContractGuard - Dashboard'
            },
            {
                path: 'services/:slug',
                component: ServiceDetailsComponent,
                title: 'ContractGuard - Service Details'
            }
        ]
    }
];
