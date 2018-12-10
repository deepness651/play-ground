
# Simple push notification service

A REST API to provide services such as user registration, listing registered users and sending push notifications for the registered users.

The API integrates with Push bullet API to push user notifications.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

 - Install [Java 11][java11] if you don't already have it.

[java11]: http://www.oracle.com/technetwork/java/javase/downloads/

 - This project use the [Apache Maven][maven] build system. Before getting
started, be sure to [download][maven-download] and [install][maven-install] it.
When you use Maven as described here, it will automatically download the required libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

 - Setting up push bullet

### Setting up push bullet
1. Visit https://www.pushbullet.com/ and Sign up
2. Go to Devices
   - Install Pushbullet to a device of your choice (be it a phone or a web browser)
3. Go to Settings, Account
   - Select Create Access Token
   - Store the generated access token somewhere safe

### Installing
1. Get the project
```
git clone https://github.com/deepness651/push-notification-service.git
```
2. Build and run the service using maven

```
mvn package

java -jar target/push-notification-service-1.0.0-SNAPSHOT.jar
```
You can also run the service without packaging it using 
```
mvn spring-boot:run
```
Also make sure that port 8080 is not used by another application as the application uses that port on localhost

Once the application is running, you can make calls to the API using any REST clients.

# API #

### Register user

**POST** /api/v1/users/create

Register a new user.

#### Example request 

    POST /api/v1/users/create HTTP/1.1
    Content-Type: application/json

```
    {
        "username": "aUser",
        "accessToken": "anAccessToken"
    }
```
**anAccessToken** A push bullet API access token.

#### Example response

    HTTP/1.1 201 OK
    Content-Type: application/json
    Cache-Control: private, max-age=0, s-maxage=0, no-cache, no-store, must-revalidate
    Expires: 0
    Pragma: no-cache

```
    {
        "username": "aUser",
        "accessToken": "anAccessToken",
        "creationTime": "2018-12-04T10:06:04",
        "numOfNotificationsPushed": 0
    }
```

####  Status codes

-   [201 Created](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2) – User registered
-   [400 Bad Request](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1) – Input does not match the schema
-   [409 Conflict](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10) – User already exists
------

**GET** /api/v1/users

List all registered users

#### Example request 

    GET /api/v1/users HTTP/1.1
    Content-Type: application/json

#### Example response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Cache-Control: private, max-age=0, s-maxage=0, no-cache, no-store, must-revalidate
    Expires: 0
    Pragma: no-cache

```
    [
        {
            "username": "aUser",
            "accessToken": "anAccessToken",
            "creationTime": "2018-12-04T10:42:51",
            "numOfNotificationsPushed": 1
        },
        {
            "username": "anotherUser",
            "accessToken": "anotherAccessToken",
            "creationTime": "2018-12-04T11:55:36",
            "numOfNotificationsPushed": 0
        }
    ]
```


####  Status codes

-   [200 OK](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.1) – Returns the list of registered users
-  [204 No Content](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5) – There are no registered users yet

------

**POST** /api/v1/push

Send a push notification for a registered user via push bullet.

#### Example request 

    POST /api/v1/push HTTP/1.1
    Content-Type: application/json

```
    {
        "username": "aUser",
        "title": "aTitle",
        "body": "someBody"
    }
```
 
#### Example response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Cache-Control: private, max-age=0, s-maxage=0, no-cache, no-store, must-revalidate
    Expires: 0
    Pragma: no-cache

```
	{
	  "status" : "OK",
	  "message" : "Push notification sent"
	}
```

####  Status codes

-   [200 OK](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.1) – Sent push notification
-   [400 Bad Request](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1) – Input does not match the schema
-   [400 Bad Request](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1) – The username in the Input does not exist

------

## Running the tests

You can use maven to run the tests
```
mvn test
```

## Built With

* [Spring 5+Boot](https://spring.io/) - The framework used
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Gokhan Urgun** 
