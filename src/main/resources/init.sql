DROP TABLE IF EXISTS wms_request;

CREATE TABLE wms_request ( 
   id IDENTITY NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   ip VARCHAR(15),
   request_time TIMESTAMP WITH TIME ZONE,
   request_method VARCHAR(4),
   request TEXT,
   wms_request_type VARCHAR(20),
   wms_srs INTEGER,
   wms_bbox TEXT,
   wms_width INTEGER,
   wms_height INTEGER
);

CREATE INDEX IF NOT EXISTS request_time_idx ON wms_request (request_time);
