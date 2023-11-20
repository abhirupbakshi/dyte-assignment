import { AfterViewInit, Component, ElementRef, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { AsyncValidatorFn, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatFormField } from '@angular/material/form-field';
import { MatFormFieldControl } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { LoginService } from '../service/login.service';
import { HttpStatusCode } from '@angular/common/http';
import { Constant } from '../constant';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login-view',
  templateUrl: './login-view.component.html',
  styleUrls: ['./login-view.component.css']
})
export class LoginViewComponent implements OnInit {

  username: FormControl = new FormControl('', [Validators.required]);
  password: FormControl = new FormControl('', [Validators.required]);
  credentials = false; // indentifies if login failed because of wrong credentials
  loggedIn = false;
  formError = true;
  form!: FormGroup;

  constructor(private fb: FormBuilder, private loginService: LoginService, private router: Router) {
  }

  async ngOnInit() {
    this.loginService.loginStatus.subscribe(s => {
      if (s.status) {
        this.loggedIn = true;
        this.router.navigate(['/logs']);
      } else {
        this.loggedIn = false;
      }
    });

    this.form = this.fb.group({
      username: this.username,
      password: this.password
    });
    this.form.valueChanges.subscribe(() => {
      this.formError = this.username.errors !== null || this.password.errors !== null;
    });
  }

  async onSubmit(event: Event) {
    event.preventDefault();

    this.username.markAllAsTouched();
    this.password.markAllAsTouched();

    if (!this.formError) {
      let result = await this.loginService.doLogin(this.username.value, this.password.value)

      if (!result.success) {
        if (result.status == HttpStatusCode.Forbidden || result.status == HttpStatusCode.Unauthorized) {
          this.credentials = true;
        } else {
          this.credentials = false;
        }
        return;
      }

      console.log(result.token);
      localStorage.setItem(Constant.LOCAL_STORAGE_TOKEN_KEY, result.token as string);
      this.loginService.loggedIn(result.token as string);
      this.loggedIn = true;
      this.credentials = false;
      this.router.navigate(['/logs']);
    }
  }
}
