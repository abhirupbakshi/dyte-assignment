import { AfterViewInit, Component, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { Constant } from 'src/app/constant';
import { Log, LogService, Metadata } from 'src/app/service/log.service';
import { LoginService } from 'src/app/service/login.service';
import { MetadataDialogComponent } from './metadata-dialog/metadata-dialog.component';
import { PredicateEvent } from '../predicates/predicates.component';
import { DateTime } from 'luxon';
import { Observable } from 'rxjs';
import { PredicateService } from '../../service/predicate.service';

@Component({
  selector: 'app-logs-table',
  templateUrl: './logs-table.component.html',
  styleUrls: ['./logs-table.component.css']
})
export class LogsTableComponent implements AfterViewInit {

  propertyPredicates: any;
  timestampRangePredicates?: Date[] | null;
  searchPredicate?: string;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns: string[] = ['level', 'message', 'resourceId', 'timestamp', 'traceId', 'spanId', 'commit', 'metadata'];
  dataSource = new MatTableDataSource<Log>([]);
  token: string | null | undefined;
  pageIndex = 1;
  pageLimit = 10;
  totalCount = 0;
  isLoadingResults = false;
  lastFetched: Date | null = null;

  constructor(
    private predicateService: PredicateService,
    private logService: LogService,
    private loginService: LoginService,
    private router: Router,
    private snackBar: MatSnackBar,
    public dialog: MatDialog) {
  }

  private showLogFetchErrDialog() {
    let sbRef = this.snackBar.open('Cannot Fetch Logs', 'Reload', {
      duration: 30000
    });
    sbRef.onAction().subscribe(() => {
      window.location.reload();
    })
  }

  private async showLogs(): Promise<void> {

    let o: any = {};
    if (this.timestampRangePredicates && this.timestampRangePredicates.length == 2) {
      o.past = this.timestampRangePredicates[0];
      o.future = this.timestampRangePredicates[1];

      o.past = DateTime.fromJSDate(o.past).toUTC().toString();
      o.future = DateTime.fromJSDate(o.future).toUTC().toString();
    }
    if (this.propertyPredicates) {
      o = Object.assign(o, this.propertyPredicates);
    }
    if (this.searchPredicate) {
      o.search = this.searchPredicate;
    }
    if (o.timestamp) {
      o.timestamp = DateTime.fromJSDate(o.timestamp).toUTC().toString();
    }
    if (o.metadata_parentResourceId) {
      let v = o.metadata_parentResourceId;
      delete o.metadata_parentResourceId;
      o['metadata.parentResourceId'] = v;
    }

    this.isLoadingResults = true;
    let result = await this.logService.findLogs(this.token as string, this.pageIndex, this.pageLimit, o);
    this.isLoadingResults = false;

    if (!result.success) {
      this.showLogFetchErrDialog();
      return;
    }

    let logs = result.logs?.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
    this.dataSource = new MatTableDataSource<Log>(logs as Log[]);
    this.totalCount = result.total!;
  }

  async ngAfterViewInit(): Promise<void> {
    this.loginService.loginStatus.subscribe(s => {
      if(s.status) {
        this.token = s.token;
      }
    })
    this.dataSource.paginator = this.paginator;
    this.token = localStorage.getItem(Constant.LOCAL_STORAGE_TOKEN_KEY);
    if (!this.token) {
      this.router.navigate(['/login']);
      return;
    }

    this.logService.pollLogIngestion(
      Constant.POLL_FREQUENCY,
      (time) => {
        if (!this.lastFetched || (time && (time.getTime() - this.lastFetched.getTime() > 0))) {
          this.showLogs();
          this.lastFetched = new Date();
        }
      },
      (res) => this.showLogFetchErrDialog()
    )

    this.predicateService.predicate.subscribe(p => {
      this.searchPredicate = p.search;
      this.propertyPredicates = p.log;
      this.timestampRangePredicates = p.range;
      this.showLogs();
    });
  }

  openMetadataDialog(event: Event, metadata: Metadata): void {
    this.dialog.open(MetadataDialogComponent, {
      data: metadata
    });
  }

  handlePageEvent(event: PageEvent) {
    this.pageIndex = event.pageIndex + 1;
    this.pageLimit = event.pageSize;
    this.showLogs();
  }
}
