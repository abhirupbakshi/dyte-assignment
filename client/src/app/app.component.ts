import { Component } from '@angular/core';
import { LoginService } from './service/login.service';
import { Router } from '@angular/router';
import { Constant } from './constant';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  token: string | null | undefined;
  loggedIn = false;

  constructor(private loginService: LoginService, private router: Router) {
  }

  async ngOnInit(): Promise<void> {

    this.loginService.loginStatus.subscribe(s => {
      if (s.status) {
        this.loggedIn = true;
        this.router.navigate(['/logs']);
      } else {
        this.router.navigate(['/login']);
      }
    });

    this.token = localStorage.getItem(Constant.LOCAL_STORAGE_TOKEN_KEY);
    if (!this.token || !await this.loginService.validateToken(this.token)) {
      this.loginService.notLoggedIn();
      this.loggedIn = false;
      this.router.navigate(['/login']);
      return;
    }

    this.loggedIn = true;
    this.router.navigate(['/logs']);
  }

  async handleLogout(event: Event) {
    await this.loginService.doLogout(this.token as string);
    localStorage.removeItem(Constant.LOCAL_STORAGE_TOKEN_KEY);
    this.loginService.notLoggedIn();

    this.token = null;
    this.loggedIn = false;
    this.router.navigate(['/login']);
  }
}
