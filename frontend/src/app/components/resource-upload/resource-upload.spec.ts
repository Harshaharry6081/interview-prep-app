import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceUpload } from './resource-upload';

describe('ResourceUpload', () => {
  let component: ResourceUpload;
  let fixture: ComponentFixture<ResourceUpload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceUpload],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceUpload);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
