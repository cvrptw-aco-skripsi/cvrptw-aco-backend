# CVRP API

## Calculate CVRP using ACO

- Endpoint : `/api/cvrp/aco`
- HTTP Method : `GET`
- Request Param : -
- Request Header :
  - Accept : `application/json`
- Request Body :

```json
{
  "timeWindows": [
    {
      "startTime": "10:00",
      "endTime": "15:00"
    },
    {
      "startTime": "15:00",
      "endTime": "20:00"
    }
  ],
  "nodes": [
    {
      "xPosition": 2.0,
      "yPosition": 3.3,
      "timeWindowIndex": 0
    },
    {
      "xPosition": 4.0,
      "yPosition": -3.6,
      "timeWindowIndex": 1
    }
  ],
  "vehicleCapacity": 1,
  "maxNumberOfVehicle": 10
}
```

- Response Body (Success) :

```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "numberOfUsedVehicle": 1,
    "routes": [
      {
        "nodeIndex": 0,
        "arrivalTime": "15:23"
      },
      {
        "nodeIndex": 1,
        "arrivalTime": "15:49"
      }
    ]
  },
  "errors": null
}
```

- Response Body (Failure) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "data": null,
  "errors": {
    "timeWindows.startTime": [
      "REQUIRED",
      "INVALID"
    ],
    "timeWindows.endTime": [
      "REQUIRED",
      "INVALID"
    ],
    "nodes.xPosition": [
      "REQUIRED",
      "INVALID"
    ],
    "nodes.yPosition": [
      "REQUIRED",
      "INVALID"
    ],
    "nodes.timeWindowIndex": [
      "REQUIRED",
      "INVALID"
    ],
    "vehicleCapacity": [
      "REQUIRED",
      "INVALID"
    ],
    "maxNumberOfVehicle": [
      "REQUIRED",
      "INVALID"
    ]
  }
}
```
