-- TODO:
-- //map-Aufrufe
-- oereb
-- Eigentümer
-- Fehler
-- WFS
-- dataservice (GET und POST)
-- print
-- grundbuchplanauszug
-- EWS 
-- //Reports
-- searchtext
-- getlegendgraphic
-- Standortkarte

DROP TABLE IF EXISTS wms_request_layer;
DROP TABLE IF EXISTS wms_request;
DROP TABLE IF EXISTS wfs_request;
DROP TABLE IF EXISTS document_request;
DROP TABLE IF EXISTS owner_request;
DROP TABLE IF EXISTS dataservice_request;
DROP TABLE IF EXISTS searchtext_request;
DROP TABLE IF EXISTS matomo_add_layer;
DROP TABLE IF EXISTS matomo_searchtext;
DROP SEQUENCE IF EXISTS api_log_sequence;

CREATE SEQUENCE api_log_sequence
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

CREATE INDEX IF NOT EXISTS wms_request_request_time_idx ON wms_request (request_time);
CREATE INDEX IF NOT EXISTS wms_request_wms_request_type_idx ON wms_request (wms_request_type);
CREATE INDEX IF NOT EXISTS wms_request_wms_srs_idx ON wms_request (wms_srs);
CREATE INDEX IF NOT EXISTS wms_request_wms_width_idx ON wms_request (wms_width);
CREATE INDEX IF NOT EXISTS wms_request_wms_height_idx ON wms_request (wms_height);
CREATE INDEX IF NOT EXISTS wms_request_dpi_idx ON wms_request (dpi);

CREATE TABLE wms_request_layer ( 
   id BIGINT NOT NULL PRIMARY KEY,
   request_id INTEGER,
   layer_name VARCHAR(1024),
   foreign key (request_id) references wms_request(id)
);

CREATE INDEX IF NOT EXISTS wms_request_layer_request_id_idx ON wms_request_layer (request_id);
CREATE INDEX IF NOT EXISTS wms_request_layer_layer_name_idx ON wms_request_layer (layer_name);

CREATE TABLE wfs_request ( 
   id BIGINT NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   ip VARCHAR(15),
   request_time TIMESTAMP WITH TIME ZONE,
   request_method VARCHAR(4),
   request TEXT,
   wfs_request_type VARCHAR(20),
   wfs_srs INTEGER,
   wfs_bbox TEXT,
   wfs_typename VARCHAR(1024)
);

CREATE INDEX IF NOT EXISTS wfs_request_request_time_idx ON wfs_request (request_time);
CREATE INDEX IF NOT EXISTS wfs_request_wfs_request_type_idx ON wfs_request (wfs_request_type);
CREATE INDEX IF NOT EXISTS wfs_request_wfs_srs_idx ON wfs_request (wfs_srs);
CREATE INDEX IF NOT EXISTS wfs_request_wfs_typename_idx ON wfs_request (wfs_typename);

CREATE TABLE document_request ( 
   id BIGINT NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   ip VARCHAR(15),
   request_time TIMESTAMP WITH TIME ZONE,
   request_method VARCHAR(4),
   request TEXT,
   document VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS document_request_request_time_idx ON document_request (request_time);
CREATE INDEX IF NOT EXISTS document_request_document_idx ON document_request (document);

CREATE TABLE owner_request ( 
   id BIGINT NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   ip VARCHAR(15),
   request_time TIMESTAMP WITH TIME ZONE,
   request_method VARCHAR(4),
   request TEXT,
   egrid VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS owner_request_request_time_idx ON owner_request (request_time);
CREATE INDEX IF NOT EXISTS owner_request_egrid_idx ON owner_request (egrid);

CREATE TABLE dataservice_request ( 
   id BIGINT NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   ip VARCHAR(15),
   request_time TIMESTAMP WITH TIME ZONE,
   request_method VARCHAR(4),
   request TEXT,
   dataset VARCHAR(1024),
   filter VARCHAR(1024)
);

CREATE INDEX IF NOT EXISTS dataservice_request_request_time_idx ON dataservice_request (request_time);
CREATE INDEX IF NOT EXISTS dataservice_request_dataset_idx ON dataservice_request (dataset);
CREATE INDEX IF NOT EXISTS dataservice_request_filter_idx ON dataservice_request (filter);

CREATE TABLE searchtext_request ( 
   id BIGINT NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   ip VARCHAR(15),
   request_time TIMESTAMP WITH TIME ZONE,
   request_method VARCHAR(4),
   request TEXT,
   searchtext VARCHAR(1024)
);

CREATE INDEX IF NOT EXISTS searchtext_request_request_time_idx ON searchtext_request (request_time);
CREATE INDEX IF NOT EXISTS searchtext_request_document_idx ON searchtext_request (searchtext);

CREATE TABLE matomo_add_layer ( 
   id BIGINT NOT NULL PRIMARY KEY,
   year INTEGER,
   month INTEGER,
   layername VARCHAR(1024),
   acount INTEGER
);

CREATE INDEX IF NOT EXISTS matomo_add_layer_year_idx ON matomo_add_layer (year);
CREATE INDEX IF NOT EXISTS matomo_add_layer_month_idx ON matomo_add_layer (month);
CREATE INDEX IF NOT EXISTS matomo_add_layer_layername_idx ON matomo_add_layer (layername);


CREATE TABLE matomo_searchtext ( 
   id BIGINT NOT NULL PRIMARY KEY,
   year INTEGER,
   month INTEGER,
   searchtext VARCHAR(1024),
   acount INTEGER
);

CREATE INDEX IF NOT EXISTS matomo_add_layer_year_idx ON matomo_add_layer (year);
CREATE INDEX IF NOT EXISTS matomo_add_layer_month_idx ON matomo_add_layer (month);
CREATE INDEX IF NOT EXISTS matomo_add_layer_searchtext_idx ON matomo_add_layer (searchtext);
