# sales

## Test API


### Create Alarm
```
{"userId":"sensor","alarmId":"temperature-001","alarmBegin":"2019-01-01T00:00:00Z","alarmEnd":"2019-01-01T00:00:00Z","alarmDesc":"Over heated recovered."}
```

### Update Alarm

```
curl -d '{"userId":"sensor","alarmId":"temperature-001","alarmBegin":"2019-01-01T00:00:00Z","alarmEnd":"2019-01-01T00:00:00Z","alarmDesc":"Over heated recovered."}' \
-X POST http://localhost:8000/api/alarm/update-alarm
```
or
```
ab -c 1 -n 10000 -p update-alarm.json  -T 'application/json' http://localhost:8000/api/alarm/update-alarm
```

### Retrieve Alarm
```
{"userId":"sensor","alarmId":"temperature-001","alarmBegin":"2019-01-01T00:00:00Z"}
```

