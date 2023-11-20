export class Constant {
  static readonly BASE_URL_DOMAIN = 'localhost';
  static readonly BASE_URL_PORT = '3000';
  static readonly BASE_URL = `http://${this.BASE_URL_DOMAIN}:${this.BASE_URL_PORT}`;
  static readonly LOCAL_STORAGE_TOKEN_KEY = 'token';
  static readonly POLL_FREQUENCY = 100;
}
