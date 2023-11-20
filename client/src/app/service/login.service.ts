import { HttpClient, HttpStatusCode } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Constant } from '../constant';

export interface LoginEvent {
  status: boolean,
  token?: string
}

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private loginSub = new Subject<LoginEvent>();

  constructor(private httpClient: HttpClient) { }

  loggedIn(token: string) {
    this.loginSub.next({
      status: true,
      token: token
    });
  }

  notLoggedIn() {
    this.loginSub.next({
      status: false,
    });
  }

  get loginStatus() {
    return this.loginSub.asObservable();
  }

  async doLogin(username: string, password: string): Promise<{ token: string | null, status: HttpStatusCode, success: boolean }> {
    let response = await fetch(`${Constant.BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        Authorization: `Basic ${btoa(`${username}:${password}`)}`
      }
    });
    let token = response.headers.get('token');
    let r = {
      success: response.ok,
      status: response.status,
      token: token
    }
    return r;
  }

  async doLogout(token: string) {
    let response = await fetch(`${Constant.BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    return response;
  }

  async validateToken(token: string | null): Promise<boolean> {
    if (!token) {
      return false;
    }

    let reaponse = await fetch(`${Constant.BASE_URL}/auth/verify/${token}`);
    if (!reaponse.ok) {
      return false;
    }

    return true;
  }
}
