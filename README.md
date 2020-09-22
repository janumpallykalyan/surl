# SURL

**SURL** is a Shorten URL generator and analytics Project
## Key Features

* Build with Docker Scalability feature
* Redis Powered Caching  
* LoadBalance with nginx
* Analytics for the generated short link
* Provided Api to use in Microservice architecture
* Unique id generated and stored in local storage who ever is using this service and stored in database
* Logged each event when ever user accesses short url 
* If Short URL is expired, generated user will be notified by email


### Stack

* Java8
* Spring Boot
* Spring Data MongoDB
* JUnit
* Mockito
* maven
* Docker
* Redis
* MongoDB
* React.js

### Setup and Run

You need to have Java 8 jdk, nodejs and docker installed on you system

1. Clone this repository
2. run mongo and redis services in docker by running ```docker-compose -f docker-compose.yml up -d```
3. go to ```server``` folder. run spring boot application by generating jar by command ```mvn clean install``` and run ```java -jar app.jar```
check by accessing ```http://localhost:8080/``` url.

4. go to ```web``` folder. run ```npm install``` and ```npm start``` 

2. To package jar file and create the app image execute the following command in **SURLService** directory: 
    ```
    mvn clean package docker:build
    ```
3. To run the system: 
    
    ```
    docker-compose up -d 
    ```
    
4. To shortify a url:

    ```
    curl -v -H "Content-Type: application/json" -X POST -d '{"longUrl":"www.google.com"}' http://localhost:8080/api/v1/shortify
    ``` 
5. To access your shorted url: (***KEY*** is the shorted code generated in the 4th step)
    ```
    curl -v -X GET http://localhost:8080/api/v1/KEY
    ```
6. To access analytics for a url: (***KEY*** is the shorted code generated in the 4th step)

    ```
    curl -v  -H "Content-Type: application/json" -X GET http://localhost:8080/api/v1/stat/KEY
    ```

## Running the tests

To run the tests :    `mvn test`

### API

**Get shortened URLs list:**
```
GET /api/v1/KEY
```
where ***KEY*** Is shorted link 


**Shortify a link:**

```
POST /api/v1/shortify
```
**Request Scheme:**
```
{"longUrl":"www.google.com"}
```
**Response Scheme:**

```
{
	"success": true,
	"message": "YQTH",
	"code": 0
}
```

**Get Statistics:**

```
GET /api/v1/stat/KEY
```

where ***KEY*** Is shorted link 

***Response Scheme:***
    
```
{
    "success": true,
    "message": "analytics",
    "code": 0,
    "lastAccessDate": "2018-05-12",
    "dailyAverage": 3.0,
    "max": 3,
    "min": 3,
    "totalPerYear": 3,
    "perMonth": {
        "June": 0,
        "October": 0,
        "December": 0,
        "May": 3,
        "September": 0,
        "March": 0,
        "July": 0,
        "January": 0,
        "February": 0,
        "April": 0,
        "August": 0,
        "November": 0
    },
    "byBrowsers": {
        "ie": 0,
        "fireFox": 0,
        "chrome": 2,
        "opera": 0,
        "safari": 0,
        "others": 1
    },
    "byOs": {
        "windows": 2,
        "macOs": 0,
        "linux": 0,
        "android": 0,
        "ios": 0,
        "others": 1
    }
}
```

## Running sharded mongo cluster on one machine for scalability:

Update the private IP address in mongoSetup/mongos/docker-compose.yml 
Run `./up.sh`  
This starts all the mongo instances with docker.  

Enter the mongo shell for one of the config servers:  
`mongo "mongodb://<IP>:40001"`  
Connect the three config servers into one replica set:  
`rs.initiate(  
  {  
    _id: "cfgrs",  
    configsvr: true,  
    members: [  
      { _id : 0, host : "<IP>:40001" },  
      { _id : 1, host : "<IP>:40002" },  
      { _id : 2, host : "<IP>:40003" }  
    ]  
  }  
)`  

Leave shell: `exit`  

Enter the mongo shell for one of the shard1 servers:  
`mongo "mongodb://<IP>:50001"`  
Connect the three shard1 servers into one replica set:  
`rs.initiate(  
  {  
    _id: "shard1rs",  
    members: [  
      { _id : 0, host : "<IP>:50001" },  
      { _id : 1, host : "<IP>:50002" },  
      { _id : 2, host : "<IP>:50003" }  
    ]  
  }  
)`  

Leave shell: `exit`  

Repeat for all other shards:  
`mongo "mongodb://<IP>:50004"`  
Connect the three shard2 servers into one replica set:  
`rs.initiate(  
  {  
    _id: "shard2rs",  
    members: [  
      { _id : 0, host : "<IP>:50004" },  
      { _id : 1, host : "<IP>:50005" },  
      { _id : 2, host : "<IP>:50006" }  
    ]  
  }  
)`  

Leave shell: `exit`  

Enter the mongo shell for the mongos server:  
`mongo "mongodb://<IP>:60000"`  
Connect shards to mongos:  
`sh.addShard("shard1rs/<IP>:50001,<IP>:50002,<IP>:50003")`  
`sh.addShard("shard2rs/<IP>:50004,<IP>:50005,<IP>:50006")`  

Create and select database: `use SURL`  
Enable sharding on database: `sh.enableSharding("SURL")`  
Create collection: `db.createCollection('shorturl')`
Create collection: `db.createCollection('range_partition_status')` 
Create collection: `db.createCollection('worker_status')`   
Shard collection: `sh.shardCollection("SURL.shorturl", {"keyCode": "hashed"})`  

Leave shell: `exit`  

Start Express application:  
`docker-compose up -d --build`  

Stop database servers: `./down.sh` (all data and configuration is still in docker volumes)  
Stop Application server: `docker-compose down`  







