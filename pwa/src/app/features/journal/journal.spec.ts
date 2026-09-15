import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpBackend, provideHttpClient } from '@angular/common/http';
import { FakeBackend } from '../../../testing/fake-backend';

import { JournalComponent } from './journal';

describe('JournalComponent', () => {
  let component: JournalComponent;
  let fixture: ComponentFixture<JournalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), { provide: HttpBackend, useClass: FakeBackend }],
      imports: [JournalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JournalComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('starts idle with no entries', () => {
    expect(component.recorderState()).toBe('idle');
    expect(component.entries()).toEqual([]);
  });

  it('blocks text submission when the draft is blank', () => {
    component.textDraft.set('   ');
    expect(component.canSubmitText()).toBe(false);
  });
});
