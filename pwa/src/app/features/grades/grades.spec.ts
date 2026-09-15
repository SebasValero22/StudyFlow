import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpBackend, provideHttpClient } from '@angular/common/http';
import { FakeBackend } from '../../../testing/fake-backend';

import { GradesComponent } from './grades';

describe('Grades', () => {
  let component: GradesComponent;
  let fixture: ComponentFixture<GradesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), { provide: HttpBackend, useClass: FakeBackend }],
      imports: [GradesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GradesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
