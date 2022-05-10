# Payconiq Stocks Application

This a small RESTful Stocks application with next features.  
All changes are kept in-memory (with H2 database).  
Application have some initial not-empty stocks, which will be same each time after application restart.

## REST API
Supports next REST operations:
 - *GET /api/stocks* - To get information all application stocks.  
 Example of output json:
 > [{"id":1,"name":"London Stock","currentPrice":2.0,"lastUpdate":"2019-12-11T22:58:34Z"},{"id":2,"name":"NewYork Stock","currentPrice":1.9,"lastUpdate":"2019-12-11T23:59:56Z"}]
 - *GET /api/stocks/{id}* - To get information about particular stock by id  
 Example of output json:
 > {"id":1,"name":"London Stock","currentPrice":2.0,"lastUpdate":"2019-12-11T22:58:34Z"}  
 Example of output json:
 > "[{\"price\":1.9,\"startDate\":\"2019-12-11T23:59:56Z\",\"endDate\":null},{\"price\":1.94,\"startDate\":\"2019-12-10T07:03:00Z\",\"endDate\":\"2019-12-11T23:59:56Z\"},{\"price\":1.59,\"startDate\":\"2019-12-09T21:08:47Z\",\"endDate\":\"2019-12-10T07:03:00Z\"}]"
 - *PUT /api/stocks/{id}* - To update particular stock price.   
 Example of request json with changes:
 > {"price":3.7}
 - *POST /api/stocks* - Add new Stock to application  
 Example of request json with changes:
  > {"name":"Hong-Kong Stock", "price":4.5}

## Requests Requirements
There are some requirements for the input requests:
 - Input price should be present and greater than 0. Otherwise there will be an exception.
 - Input stock name should be not-empty. Otherwise there will be an exception.
 - Input stock name should be unique (after removing trailing and leading whitespaces).  
 Otherwise there will be an exception.
 - Stock name can't be modified.

## How to build and start server
*Note*: Java 11 is required  

Command to build with maven:  
> mvn clean install 

Or if you want to skip tests:  
> mvn clean install -DskipTests

Command to start after build (on port 8080):  
> java -jar target/payconiq-assignment-1.0.jar

To start server on a specific port:
> java -jar target/payconiq-assignment-1.0.jar --server.port=8081
