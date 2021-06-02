DROP TABLE IF EXISTS wms_request_layer;
DROP TABLE IF EXISTS wms_request;
DROP SEQUENCE IF EXISTS ows_log_sequence;

CREATE SEQUENCE ows_log_sequence
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1;

CREATE TABLE wms_request ( 
   id BIGINT NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   ip VARCHAR(15),
   request_time TIMESTAMP WITH TIME ZONE,
   request_method VARCHAR(4),
   request TEXT,
   wms_request_type VARCHAR(20),
   wms_srs INTEGER,
   wms_bbox TEXT,
   wms_width INTEGER,
   wms_height INTEGER,
   dpi DOUBLE
);

CREATE INDEX IF NOT EXISTS request_time_idx ON wms_request (request_time);
CREATE INDEX IF NOT EXISTS wms_request_type_idx ON wms_request (wms_request_type);
CREATE INDEX IF NOT EXISTS wms_srs_idx ON wms_request (wms_srs);
CREATE INDEX IF NOT EXISTS wms_width_idx ON wms_request (wms_width);
CREATE INDEX IF NOT EXISTS wms_height_idx ON wms_request (wms_height);
CREATE INDEX IF NOT EXISTS dpi_idx ON wms_request (dpi);

CREATE TABLE wms_request_layer ( 
   id BIGINT NOT NULL PRIMARY KEY,
   request_id INTEGER,
   layer VARCHAR(255),
   foreign key (request_id) references wms_request(id)
);

CREATE INDEX IF NOT EXISTS request_id_idx ON wms_request_layer (request_id);
CREATE INDEX IF NOT EXISTS layer_idx ON wms_request_layer (layer);
