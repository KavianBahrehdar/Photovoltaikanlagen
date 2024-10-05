-- A database vektordatenverarbeitung has to be created first. This can be done via pgadmin or psql.
-- The script also can be executed via psql e.g. psql -U postgres -f exercise_1.sql vektordatenverarbeitung
CREATE DATABASE Geoinformatik2
WITH OWNER = postgres;
CREATE SCHEMA osm;
SET search_path TO osm;

CREATE EXTENSION postgis SCHEMA osm;
SELECT postgis_full_version();

CREATE TABLE pois(
    id UUID PRIMARY KEY, 
    name text, 
    properties jsonb, 
    geom geometry(POINT, 4326)
);

CREATE TABLE polygon(
    id UUID PRIMARY KEY,
     name text,
      properties jsonb,
       geom geometry(Linestring, 4326)
);

CREATE TABLE hexagon(
    id UUID PRIMARY KEY,
     name text,
      properties jsonb,
       geom geometry(Linestring, 4326)
);