# Auxby Users Manager Service

Spring boot service to create, read, update, delete users on Keycloak. Also send a
verification and reset-password link to the users.

![Coverage](badges/jacoco.svg) ![Branches](badges/branches.svg)

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
    * ${KEYCLOAK_URL} - the keyclaok host url 
    * ${KEYCLOAK_REALM} - the keyclaok realm
    * ${KEYCLOAK_CLIENT_ID} - the keyclaok client id
    * ${KEYCLOAK_CLIENT_SECRET} - the keyclaok client secret
    * ${KEYCLOAK_AUTH_URL} - the keyclaok authentication endpoint
    

# Deployment

* Run : <code>mvn clean package</code>
* Override : <code>deployment/auxby-user-manager.jar</code> with <code> targer/auxby-user-manager.jar</code>
* Go on RENDER and manual trigger a deployment
