# Auxby Users Manager Service

Spring boot service to create, read, update, delete users on Keycloak. Also send a
verification and reset-password link to the users.

# Compile and Build

    mvn clean install

# Configuration


* Database configuration
    * ${DB_HOST} - the database url
    * ${DB_PORT} - the database port
    * ${DB_NAME} - the database name
    * ${DB_USER} - the database username
    * ${DB_PASSWORD} - the database password

 
