import { HttpBackend, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';

/**
 * Instant-success backend for unit tests: every request resolves immediately
 * with an empty array body, so components can mount without a live API.
 */
export class FakeBackend implements HttpBackend {
  handle(_req: HttpRequest<never>): Observable<HttpResponse<unknown>> {
    return of(new HttpResponse({ status: 200, body: [] }));
  }
}
