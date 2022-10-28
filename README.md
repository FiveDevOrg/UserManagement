# Auxby Users Manager Service

Spring boot service to create, read, update, delete users on Keycloak. Also send a
verification and reset-password link to the users.

![Coverage](badges/jacoco.svg)

# Compile and Build

    mvn clean install

# Configuration


* Database configuration
    * ${DB_HOST} - the database url
    * ${DB_PORT} - the database port
    * ${DB_NAME} - the database name
    * ${DB_USER} - the database username
    * ${DB_PASSWORD} - the database password
    * ${USER_MANAGER_DOMAIN_NAME} - the domain name (e.g: localhost)
    * ${EUREKA_URL} - the eureka URL
    * ${EUREKA_USER_PASSWORD} - password used by eureka

# Deployment

* Run : <code>mvn clean package</code>
* Override : <code>deployment/auxby-user-manager.jar</code> with <code> targer/auxby-user-manager.jar</code>
* Go on RENDER and manual trigger a deployment
