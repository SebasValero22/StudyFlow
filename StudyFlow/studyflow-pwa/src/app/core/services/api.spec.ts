import { TestBed } from '@angular/core/testing';

// 1. Importamos ApiService en lugar de Api
import { ApiService } from './api';

describe('ApiService', () => {
  // 2. Cambiamos el tipo de la variable
  let service: ApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    // 3. Inyectamos ApiService
    service = TestBed.inject(ApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
