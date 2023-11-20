import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataDialogComponent } from './metadata-dialog.component';

describe('MetadataDialogComponent', () => {
  let component: MetadataDialogComponent;
  let fixture: ComponentFixture<MetadataDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MetadataDialogComponent]
    });
    fixture = TestBed.createComponent(MetadataDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
