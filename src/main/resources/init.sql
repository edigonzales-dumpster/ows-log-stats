DROP TABLE IF EXISTS wms_request;

CREATE TABLE wms_request ( 
   id IDENTITY NOT NULL PRIMARY KEY,
   md5 VARCHAR(255) NOT NULL UNIQUE,
   title VARCHAR(50) NOT NULL, 
   author VARCHAR(20) NOT NULL, 
   submission_date DATE 
);