import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { PredicateEvent } from '../logs-view/predicates/predicates.component';

@Injectable({
  providedIn: 'root'
})
export class PredicateService {

  private predicateSub = new Subject<PredicateEvent>();

  emit(event: PredicateEvent) {
    this.predicateSub.next(event);
  }

  get predicate() {
    return this.predicateSub.asObservable();
  }
}
