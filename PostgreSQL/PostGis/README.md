Diese �bung baut auf die Datenbankstruktur aus �bung 5 auf. 
Im einzelnen wird zur Ausf�hrung des (Integration-) Tests folgendes vorausgesetzt:
- Es existiert eine Datenbank "vektordatenverarbeitung"
- Die Datenbank erlaubt den Zugriff �ber den default User (postgres) und das default Password (postgres)  
- Es existiert ein Schema "osm" in der Datenbank "vektordatenverarbeitung" -> Create schema osm; SET search_path TO osm;
- Die PostGIS Extension f�r das Schema "osm" erzeugt wurde -> CREATE EXTENSION postgis SCHEMA osm;
- Es existiert eine Tabelle "pois" -> "CREATE TABLE pois(id UUID PRIMARY KEY, name text, geom geometry(POINT, 4326));"