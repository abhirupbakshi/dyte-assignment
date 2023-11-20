import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Metadata } from 'src/app/service/log.service';

@Component({
  selector: 'app-metadata-dialog',
  templateUrl: './metadata-dialog.component.html',
  styleUrls: ['./metadata-dialog.component.css']
})
export class MetadataDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: Metadata) {}
}
