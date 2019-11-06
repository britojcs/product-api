# A PRODUCT API FOR CRUD OPERATIONS - Spring Boot Microservices

This project demonstrates basic CRUD operations for a Product with the following features:

- Create a single product
- Update a product
- Delete a product
- Fetch a product by id
- Fetch all products
- Search by all product fields
- Batch creation of products
- Sorting on all fields
- Pagination

## Architecture

- **Spring Boot** : Microservices
- **Eureka** : Service discovery
- **Zuul** : API gateway
- **Ribbon** : Service load balancer
- **JWT** : Authentication
- **Docker** : Container

## Microservices

There are 4 micro services
- **auth-service** :  Authenticates user and provides a JWT bearer token
- **eureka-server** : All services register with a eureka server for discovery
- **zuul-server** : Provides gateway and token validation services
- **catalog-service** : API for products

### Implementation Specifics

- Data Access : Spring Data JPA
- Database : In memory H2 
- Authentication : JWT In Memory (user, admin)
- Secured writes using JWT tokens, reads are open
- Build, Deploy and Run : Maven and Docker
- API Docs and Samples : Postman, available at https://documenter.getpostman.com/view/8196426/SVYjVNsA?version=latest
- Versioned API using Content Type Negotiation (Accept header)
- Response formats : HAL (application/hal+json) and Basic JSON (application/json) 

### EndPoints ###

| Service       | Direct EndPoint             		    | Gateway                   	          | Method | Description                         |
| ------------- | ------------------------------------- | --------------------------------------- | ------ | ----------------------------------- |
| catalog       | /products     						| /api/products  	   					  | GET    | Get all products                    |
| catalog       | /products		          				| /api/products		  					  | POST   | Add a product                       |
| catalog       | /products/{id}     					| /api/products/{id}   					  | PUT    | Update a product                    |
| catalog       | /products/{id}          				| /api/products/{id}  					  | PATCH  | Update a product                    |
| catalog       | /products/{id}      					| /api/products/{id}   					  | DELETE | Delete a product                    |
| catalog       | /products/batch           			| /api/products/batch  					  | POST   | Add a batch of products             |
| auth          | /							  		    | /auth								 	  | GET    | Authenticate and get a bearer token |


## Build & Run

### Docker

- **mvn clean package** : build the services
- **docker-compose up --build** : build docker images and containers, run the containers.
- **docker-compose stop** : stop all services

### IDE

- Clone and Import Maven projects in IDE of your choice
- Clean and build projects
- Update the *>eureka:serviceUrl:defaultZone:* property to *>http://localhost:8761/eureka/* in the application.yml file for zuul-server, auth-service and catalog-service
- Start eureka-server : runs at port 8761
- Start zuul-server : runs at port 8762
- Start auth-server : runs at port 8763
- Start catalog-service : runs at 8100
- Start one more instance of catalog service on port 8200

For API Docs and Postman tests, please refer to https://documenter.getpostman.com/view/8196426/SVYjVNsA?version=latest

## Test Round Robin Load Balance of catalog service
Request 1 : http://localhost:8762/api/
Returns : Catalog Service running at port: 8100

Request 2 : http://localhost:8762/api/
Returns : Catalog Service running at port: 8200