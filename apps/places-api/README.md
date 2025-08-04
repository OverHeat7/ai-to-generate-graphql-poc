### Spring Boot and PostGIS tutorial

This project demonstrate how you can use PostGIS and Spring Boot to manage spatial data

#### Run PostGIS
```
docker-compose up -d --profile (all|places-api) # Choose only one profile
```

### Load the Data

```
ogr2ogr -f "PostgreSQL" PG:"dbname=postgis user=postgis host=localhost port=5435 password=postgis" "src/main/resources/pois.geojson" -sql "select cast(ID as INTEGER), name, country, state, formattedAddress, open24h, services from pois" -nln pois
```

### Buid the applicaion
```
mvn clean install
```

### Run the application
```
mvn spring-boot:run
```


### Test the REST API

Open your browser at <http://localhost:8980/city/48.232509500000106/-101.29627319999997/200000>


- 1st parameter: latitude in degree
- 2nd parameter: longitude in degree
- 3rd parameter: distance in meter


This will return all the cities within the distance around the specified location.


### Access the database

```
psql -h localhost -p 5435 -d postgis -U postgis
```

