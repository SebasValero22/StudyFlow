import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpBackend, provideHttpClient } from '@angular/common/http';
import { FakeBackend } from '../../../testing/fake-backend';

import { SettingsComponent } from './settings';

describe('Settings', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), { provide: HttpBackend, useClass: FakeBackend }],
      imports: [SettingsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
