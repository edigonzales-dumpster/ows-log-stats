# ows-log-stats

## todo
- schema support
- h2 postgres mode, damit es auch mit postgres funktioniert?

## sql
```
SELECT 
    count(LAYER_NAME),
    LAYER_NAME 
FROM 
    WMS_REQUEST_LAYER 
GROUP BY
    LAYER_NAME 
ORDER BY 
    count(LAYER_NAME) DESC
```

```
SELECT 
    count(LAYER_NAME),
    LAYER_NAME 
FROM 
    WMS_REQUEST_LAYER 
WHERE 
    LAYER_NAME LIKE '%ekat%'
GROUP BY
    LAYER_NAME 
ORDER BY 
    count(LAYER_NAME) DESC
```