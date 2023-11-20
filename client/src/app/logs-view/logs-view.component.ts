import { Component, Input, OnInit } from '@angular/core';
import { PredicateEvent } from './predicates/predicates.component';
import { Observable } from 'rxjs';
import { LoginService } from '../service/login.service';
import { Constant } from '../constant';
import { Route, Router } from '@angular/router';

@Component({
  selector: 'app-logs-view',
  templateUrl: './logs-view.component.html',
  styleUrls: ['./logs-view.component.css']
})
export class LogsViewComponent implements OnInit {

  @Input() loggedIn = false;

  constructor(private loginService: LoginService, private router: Router) {
  }

  async ngOnInit(): Promise<void> {

    this.loginService.loginStatus.subscribe(s => {
      if (s.status) {
        this.loggedIn = true;
      } else {
        this.loggedIn = false;
        this.router.navigate(['/login']);
      }
    });

    let token = localStorage.getItem(Constant.LOCAL_STORAGE_TOKEN_KEY);
    if (token) {
      this.loggedIn = true;
    }
  }
}
