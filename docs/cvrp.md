# CVRP API

## Calculate CVRP using ACO

- Endpoint : `/api/cvrp/aco`
- HTTP Method : `POST`
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
      "x": 2.0,
      "y": 3.3,
      "timeWindowIndex": 0,
      "demand": 3.0
    },
    {
      "x": 4.0,
      "y": -3.6,
      "timeWindowIndex": 1,
      "demand": 4.0
    }
  ],
  "vehicleCapacity": 9.0,
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
    "totalDistance": 12.3,
    "vehicles": [
      {
        "nodes": [
          {
            "id": 1,
            "x": 2.0,
            "y": 3.3,
            "timeWindowIndex": 0,
            "demand": 3.0,
            "visited": true
          }
        ],
        "arrivalTimes": [
          "10:00"
        ]
      }
    ]
  },
  "errors": null
}
```

- Response Body (Failure - Validation errors) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "data": null,
  "errors": {
    "timeWindows": [
      "REQUIRED",
      "INVALID"
    ],
    "timeWindows[0].startTime": [
      "REQUIRED",
      "INVALID"
    ],
    "timeWindows[0].endTime": [
      "REQUIRED",
      "INVALID"
    ],
    "nodes": [
      "REQUIRED",
      "INVALID"
    ],
    "nodes[0].x": [
      "REQUIRED",
      "INVALID"
    ],
    "nodes[0].y": [
      "REQUIRED",
      "INVALID"
    ],
    "nodes[0].timeWindowIndex": [
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

- Response Body (Failure - Business Error) :

```json
{
  "code": 400,
  "status": "Bad Request",
  "data": null,
  "errors": {
    "solution": [
      "NOT_FOUND"
    ]
  }
}
```
