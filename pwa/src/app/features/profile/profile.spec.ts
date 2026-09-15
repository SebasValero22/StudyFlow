import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpBackend, provideHttpClient } from '@angular/common/http';
import { FakeBackend } from '../../../testing/fake-backend';

import { ProfileComponent } from './profile';

describe('Profile', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), { provide: HttpBackend, useClass: FakeBackend }],
      imports: [ProfileComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
