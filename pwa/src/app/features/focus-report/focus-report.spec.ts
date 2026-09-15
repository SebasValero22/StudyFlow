import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpBackend, provideHttpClient } from '@angular/common/http';
import { FakeBackend } from '../../../testing/fake-backend';

import { FocusReportComponent } from './focus-report';

describe('FocusReport', () => {
  let component: FocusReportComponent;
  let fixture: ComponentFixture<FocusReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), { provide: HttpBackend, useClass: FakeBackend }],
      imports: [FocusReportComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FocusReportComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default date range set', () => {
    expect(component.fromDate).toBeTruthy();
    expect(component.toDate).toBeTruthy();
  });
});
