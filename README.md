It is a very simple log monitoring app where the frontend has been built win Angular and backend with Spring framework.

# Directory structure

- client directory: contains the frotend
- logging directory: contains the backend

# API Documentation
In the following section, it is assumed that the base URL is 'URL'.
- URL/ (POST): To add a log. For adding no authentication is needed.
- URL/last-ingested (GET): To know when was the last time a log was added to the database. can be used for polling purposes
- URL/auth/login (POST): To login. The response sends back a JWT token in the header with name 'token'
- URL/auth/logout (POST): To login.
- URL/auth/verify/{token} (GET): To verify if a JWT token valid or not
- URL/ (GET): This is the main api for this app. This api mainly uses query parameters to to its work. There are three catagories of parameters:
- - Search parameter: written as, search=**search_value**, with this param we can search by all the 'text' and 'log level' fields combined.
- - Timestamp range filter: For this we need to pass 2 parameters: past and future, both having a UTC time stamp value. Then it returns the results whose timestamps are in between those two (inclusive). If only one is given, a bad request will occur.
- - property filteration: We can also give property names as query parameter and their values, by which we want to filter the results. 
- All of the above mentioned filteration features works together also. If multiple filters are given, they are treated as a conjunction (AND operation).
- The authentication is needed so that sensitive logs can be kept away from unwanted peoples. We can configure the app for this. Right now this configuration is only limited to log levels, i.e., in a log hierarchy upto a certain level the user can see the logs. for example: a user maybe able to see logs from level Fatal to Warn, but not above that. An admin might be able to see all of them.

# Present Configuration
With present configuration, the app assumes there are two types of users: an admin and a developer. An admin can see all of the logs. A developer will be able to see upto Warn level. When the backend app starts, it executes a sql file: import.sql to add 2 users for testing - an admin and a developer. Their username and passwords are:
- Admin: admin:password
- Developer: developer:password

# Running the app

This app uses SQL database and a ORM framework - hibernate. Meaning that you can swap out your own choice of database. In my machine, the app has been ested with mariadb. Also, for changing the database, one needs to update the application.properties files and include JDBC drivers accordingly.

In the backed part, CORS's access control alowed origin has been set to http://localhost:5000, so running the frontend on this origing will be a good idea.

## Runnung the backend

The backend has been configured to run on port 3000. you can change that in application.properties
To run the backend, first open a terminal the project's root directry and execute the maven (a build tool) command: 
- ./mvnw spring-boot:run (for unix)
- ./mvnw.cmd spring-boot:run (for windows)

It will build the app as well as run it.

## Running the frontend

The frontend has been build using Angular. Angular app can be run either in development mode or in production mode. For development mode, go to root directory of client and run:
- npm i
- npx ng serve --port 5000

For production mode, I have already included a lightweight server named: http-server. With it you can run the compiled project. After compilation (npx ng build) make sure to dist/client directory (it is where the compiled files will be placed), copy the index.html and change the copied file name to 404.html. Then run:
- http-server dist/client --port 5000

# Features

- This app uses polling to know when to fetch data.
- The frontend has been build with angular with material ui
- The backend has been build with spring framework

