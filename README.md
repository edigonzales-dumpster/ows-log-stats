# ows-log-stats

## todo
- schema support
- h2 postgres mode, damit es auch mit postgres funktioniert?
- referer (?)
- abstract request class
- query nur mit getmap
- print get nicht (wegen post?)

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

```
SELECT
    request_date,
    DAYNAME(request_date) AS name_of_day,
    total_request
FROM 
(
    SELECT 
        CAST(REQUEST_TIME AS DATE) AS request_date,
        count(*) AS total_request
    FROM 
        WMS_REQUEST 
    GROUP BY
        request_date
    ORDER BY 
        request_date ASC
) AS foo
```

```
SELECT 
    count(*) AS sum_doc,
    DOCUMENT 
FROM 
    DOCUMENT_REQUEST
GROUP BY 
    DOCUMENT 
ORDER BY 
    sum_doc DESC
```

```
SELECT
    request_date,
    DAYNAME(request_date) AS name_of_day,
    total_request
FROM 
(
    SELECT 
        CAST(REQUEST_TIME AS DATE) AS request_date,
        count(*) AS total_request
    FROM 
        DOCUMENT_REQUEST 
    GROUP BY
        request_date
    ORDER BY 
        request_date ASC
) AS foo
```

```

```
