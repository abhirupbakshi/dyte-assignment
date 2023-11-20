import { Injectable } from '@angular/core';
import { Constant } from '../constant';
import { HttpStatusCode } from '@angular/common/http';

export enum Level {
  TRACE='TRACE',
  DEBUG='DEBUG',
  INFO='INFO',
  WARN='WARN',
  ERROR='ERROR',
  FATAL='FATAL'
}

export interface Metadata {
  parentResourceId: string;
}

export interface Log {
  uuid: string;
  level: Level;
  message: string;
  resourceId: string;
  timestamp: Date,
  traceId: string;
  spanId: string;
  commit: string;
  metadata: Metadata;
}

interface FindLogsResponse {
  success: boolean;
  status: HttpStatusCode;
  logs: Log[] | null;
  total: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class LogService {

  constructor() { }

  private stringToLevel(value: string): Level | null {
    switch (value.toLowerCase()) {
      case 'TRACE'.toLowerCase():
        return Level.TRACE
        break;
      case 'DEBUG'.toLowerCase():
        return Level.DEBUG
        break;
      case 'INFO'.toLowerCase():
        return Level.INFO
        break;
      case 'WARN'.toLowerCase():
        return Level.WARN
        break;
      case 'ERROR'.toLowerCase():
        return Level.ERROR
        break;
      case 'FATAL'.toLowerCase():
        return Level.FATAL
      default:
        return null;
    }
  }

  async findLogs(token: string, page: number, limit: number, queries?: any): Promise<FindLogsResponse> {
    let params = new URLSearchParams();
    params.append('page', `${page}`);
    params.append('limit', `${limit}`);

    for (const key in queries) {
      if (queries.hasOwnProperty(key)) {
        params.append(key, queries[key]);
      }
    }

    let response = await fetch(`${Constant.BASE_URL}?${params.toString()}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    let logs = response.ok ? await response.json() as Log[] : null;
    let total = response.headers.get('X-Total-Count');
    let b = true;

    if (logs) {
      logs = logs.map(l => {
        let level = this.stringToLevel(l.level);
        b = level != null;

        if (b) {
          l.timestamp = new Date(l.timestamp);
          l.level = level as Level;
        }
        return l;
      });
    }
    b = b && !!total;

    return {
      success: response.ok,
      status: response.status,
      logs: b ? logs : null,
      total: b ? +total! : null
    };
  }

  async pollLogIngestion(ms: number, success: (timestamp: Date | null) => void, error?: (res: Response | null) => void) {
    setInterval(async () => {
      let response = await fetch(`${Constant.BASE_URL}/last-ingested`);
      if (!response.ok) {
        if (error) {
          error(response);
        }
        return;
      }

      let body = await response.json();
      let timestamp = body.timestamp == null ? null : new Date(body.timestamp);
      success(timestamp);
    }, ms);
  }
}
