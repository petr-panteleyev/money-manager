# Docker Configuration

This directory contains docker compose file that brings up MySQL server configured
for Money Manager application.

## Initialization

* (Optional) Edit [docker-compose.yml](docker-compose.yml) to change local port
and other parameters as required

* Run docker container:<br>
```$ docker-compose up -d```

* In the application add new connection profile with the following attributes:

|Attribute|Value|
|---|---|
|Server|localhost|
|Port|3310|
|Login|money|
|Password|money|
|Schema|money|

* Press ```Create``` button to initialize tables.

