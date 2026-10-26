import { ApplicationConfig, provideBrowserGlobalErrorListeners, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideMarkdown } from 'ngx-markdown';

import { routes } from './app.routes';
import { provideServiceWorker } from '@angular/service-worker';
import { authInterceptor } from './core/interceptors/auth.interceptor';

function isDesktopTauri(): boolean {
  if (typeof window === 'undefined') return false;
  return (
    !!(window as any).__TAURI_INTERNALS__ ||
    !!(window as any).__TAURI__ ||
    window.location.hostname === 'tauri.localhost' ||
    window.location.origin.includes('tauri')
  );
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideMarkdown(),
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode() && !isDesktopTauri(),
      registrationStrategy: 'registerWhenStable:30000'
    })
  ]
};
