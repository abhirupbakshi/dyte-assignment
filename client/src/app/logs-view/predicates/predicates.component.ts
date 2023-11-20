import { AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarRef } from '@angular/material/snack-bar';
import { DateTime } from 'luxon';
import { ECalendarValue } from 'ng2-date-picker';
import { Observable } from 'rxjs';
import { Level, Metadata } from 'src/app/service/log.service';
import { PredicateService } from '../../service/predicate.service';

export interface PredicateEvent {
  log: any,
  range: Date[] | null,
  search?: string
}

class LogFormControl {
  uuid = new FormControl();
  level = new FormControl();
  message = new FormControl();
  resourceId = new FormControl();
  timestamp = new FormControl();
  traceId = new FormControl();
  spanId = new FormControl();
  commit = new FormControl();
  metadata_parentResourceId = new FormControl();
}

class TimestampRange {
  past = new FormControl();
  future = new FormControl();
}

@Component({
  selector: 'app-predicates',
  templateUrl: './predicates.component.html',
  styleUrls: ['./predicates.component.css'],
})
export class PredicatesComponent implements AfterViewInit {

  private searchStr?: string;
  private readonly shadowLog: any = {};
  private readonly shadowTimeRange: any = {};
  private readonly m: Map<string, MatSnackBarRef<any>> = new Map();

  searchStrFc = new FormControl();
  timeRange = new TimestampRange();
  logForm = new LogFormControl();
  datePickerConfig = {}

  constructor(private matSnackBar: MatSnackBar, private predicateService: PredicateService) {
  }

  private removePreviousSnackBar(m: Map<string, MatSnackBarRef<any>>, property: string) {
    if (m.has(property)) {
      m.get(property)?.dismiss();
      m.delete(property);
    }
  }

  ngAfterViewInit(): void {

    Object.keys(this.logForm).forEach(key => {
      let fc = this.logForm[key as keyof LogFormControl];

      if (key == 'uuid') {

        fc.valueChanges.subscribe(value => {
          this.removePreviousSnackBar(this.m, key);
          if (value && !this.isValidUUID(value)) {
            this.m.set(key, this.showErrDialog('Invalid UUID'));
            return;
          }
          this.shadowLog[key] = value;
        });

      } else if (key == 'level') {

        fc.valueChanges.subscribe(value => {
          this.removePreviousSnackBar(this.m, key);
          if (value && !this.isValidLevel(value)) {
            this.m.set(key, this.showErrDialog('Log level should be from: TRACE, DEBUG, INFO, WARN, ERROR, FATAL'));
            return;
          }
          this.shadowLog[key] = value;
        });

      } else if (key == 'timestamp') {

        fc.valueChanges.subscribe(value => {
          this.removePreviousSnackBar(this.m, key);
          if (value && !this.isValidDate(value)) {
            this.m.set(key, this.showErrDialog('Invalid timestamp'));
            return;
          }
          this.shadowLog[key] = value ? new Date(value) : null;
        });

      } else {
        fc.valueChanges.subscribe(value => this.shadowLog[key] = value);
      }
    });

    Object.keys(this.timeRange).forEach(key => {
      let fc = this.timeRange[key as keyof TimestampRange];

      fc.valueChanges.subscribe(value => {
        this.removePreviousSnackBar(this.m, key);
        if (value && !this.isValidDate(value)) {
          this.m.set(key, this.showErrDialog('Invalid timestamp'));
          return;
        }
        this.shadowTimeRange[key] = value ? new Date(value) : null;
      });
    });

    this.searchStrFc.valueChanges.subscribe(value => {
      this.searchStr = value;
    });
  }

  private showErrDialog(msg: string) {
    return this.matSnackBar.open(msg, '', {
      duration: 5000
    })
  }

  private isValidUUID(uuid: string): boolean {
    const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidPattern.test(uuid);
  }

  private isValidLevel(value: string): boolean {
    return Object.values(Level).map(v => v.toLowerCase()).includes(value.toLowerCase() as Level);
  }

  private isValidDate(value: string): boolean {
    const timestamp = Date.parse(value);
    return !isNaN(timestamp);
  }

  private sanitaize() {

    let arr = []
    for (let key in this.shadowLog) {
      if (!this.shadowLog[key]) {
        arr.push(key);
      }
    }
    for (let e of arr) {
      delete this.shadowLog[e];
    }

    let past = this.shadowTimeRange.past;
    let future = this.shadowTimeRange.future;

    if (!past) {
      delete this.shadowTimeRange['past'];
    }
    if (!future) {
      delete this.shadowTimeRange['future'];
    }
    if ((past && !future) || (!past && future)) {
      if (!past) {
        this.removePreviousSnackBar(this.m, 'pastMissing');
        this.m.set('pastMissing', this.showErrDialog('Past timestamp is missing'));
      }
      if (!future) {
        this.removePreviousSnackBar(this.m, 'futureMissing');
        this.m.set('futureMissing', this.showErrDialog('Future timestamp is missing'));
      }
    } else {
      this.removePreviousSnackBar(this.m, 'pastMissing');
      this.removePreviousSnackBar(this.m, 'futureMissing');
    }
  }

  onSubmit(event: SubmitEvent) {
    event.preventDefault();
    this.sanitaize();
    let l = Object.keys(this.shadowTimeRange).length;

    if (l != 1) {
      let p = {
        log: this.shadowLog,
        range: l == 0 ? null : [this.shadowTimeRange.past, this.shadowTimeRange.future] as Date[],
        search: this.searchStr
      };
      this.predicateService.emit(p);
    }
  }
}
