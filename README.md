# CouponManager

CouponManager is a microservice responsible for managing the creation, distribution, and application of discount coupons
on the TicketFlow platform. It allows adding, removing, updating, and searching for coupons, as well as applying them to
ticket purchases to provide discounts to users.

### Usage

To use the CouponManager, you need to perform operations such as creating, updating, deleting, and searching for
coupons. Additionally, you'll be able to apply coupons to ticket purchases to provide discounts to users.

The CouponManager service communicates with other services in the TicketFlow ecosystem using Eureka for service
discovery and RabbitMQ for message passing. It leverages reactive programming with Spring WebFlux and MongoDB to handle
non-blocking data access and provide a highly scalable solution.

### Getting Started

To successfully execute the CouponManager microservice, it is crucial to ensure that the Discovery and Configuration
services are up and running. These services facilitate seamless communication and coordination among the different
components of the TicketFlow platform.

#### Prerequisites

1. Ensure that the **Eureka Discovery Server** is active, enabling service discovery and registration.
2. Make sure that the **Spring Cloud Config Server** is operational, providing centralized configuration management for
   distributed systems.
   Building and Running the Docker Image
   Before you can run the CouponManager microservice using Docker, you need to build the Docker image. To do this,
   follow the steps below:

Open a terminal or command prompt and navigate to the root directory of the CouponManager project.
Execute the following command to build the Docker image:

```mvn clean install jib:dockerBuild```

This command will clean the project, install the necessary dependencies, and build the Docker image using the Jib plugin. 
Once the Docker image has been built, you can now run the CouponManager microservice using Docker Compose or any other container orchestration platform of your choice.

### Key Dependencies

The CouponManager is built using cutting-edge technologies and libraries that enable high performance, scalability, and
maintainability:

* **Spring Boot**: The foundation of the service, offering ease of development and out-of-the-box support for various
  features.
* **Spring WebFlux**: Enables reactive and non-blocking web applications, promoting high concurrency and efficient
  resource utilization.
* **Reactive MongoDB**: Provides asynchronous and non-blocking communication with MongoDB, ensuring high performance and
  scalability.
* **Eureka Client**: Facilitates service discovery and registration, allowing seamless integration with other
  microservices in the TicketFlow ecosystem.
* **Spring Cloud Config**: Integrates the Config Server, enabling centralized configuration management for distributed
  systems.



