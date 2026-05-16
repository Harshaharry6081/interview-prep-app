import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MockInterview } from './mock-interview';

describe('MockInterview', () => {
  let component: MockInterview;
  let fixture: ComponentFixture<MockInterview>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MockInterview],
    }).compileComponents();

    fixture = TestBed.createComponent(MockInterview);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
