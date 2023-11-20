import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LogsViewComponent } from './logs-view/logs-view.component';
import { LoginViewComponent } from './login-view/login-view.component';

const routes: Routes = [
  { path: 'login', component: LoginViewComponent },
  { path: 'logs', component: LogsViewComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
