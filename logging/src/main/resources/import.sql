INSERT INTO roles (name) VALUES ('ADMIN'), ('DEVELOPER');

INSERT INTO users (account_non_expired, account_non_locked, credentials_non_expired, enabled, created_at, email, forename, password, surname, username) VALUES (1, 1, 1, 1, '2023-11-18 08:50:27.259', 'admin@email.com', 'Admin', '$2a$10$AmIMqolOcqVc1Qk.u0feluhKLV6D4vaXgAFpBhRWuzH9zo4pOWIQq', 'User', 'admin');
INSERT INTO users (account_non_expired, account_non_locked, credentials_non_expired, enabled, created_at, email, forename, password, surname, username) VALUES (1, 1, 1, 1, '2023-11-18 08:50:27.259', 'developer@email.com', 'Developer', '$2a$10$jSIu2XZLaAEif5evqHvnY./JFXPnXWb.MauCBjD28VWF1IQlgMzLu', 'User', 'developer');
INSERT INTO users_roles (role_name, username) VALUES ('ADMIN', 'admin');
INSERT INTO users_roles (role_name, username) VALUES ('DEVELOPER', 'developer');
