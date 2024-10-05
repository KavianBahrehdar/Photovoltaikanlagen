package com.thd.mapserver.infrastructure.controller;

import java.sql.*;
import java.util.*;

//import org.json.JSONObject;
//import org.json.JSONException;
//import net.minidev.json.JSONObject;
//import org.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thd.mapserver.domain.geom.Curve;
import com.thd.mapserver.domain.geom.LineString;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thd.mapserver.domain.Feature;
import com.thd.mapserver.domain.geom.Point;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class FeatureCollectionsController {
//	private static List<Feature> features = List.of(new Feature("fid1", new Point(10, 10, 321), "feature1"),
//			new Feature("fid2", new Point(15, 11, 32), "feature1"),
//			new Feature("fid3", new Point(17, 11, 32), "feature3"));


    @Operation(tags = "Features", summary = "retrieve all features")
    @GetMapping(value = "/features", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Feature> getFeatures() {

        List<Feature> features = new ArrayList<>();
        String connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";

        try (Connection connection = DriverManager.getConnection(connectionString);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, properties, ST_X(geom) AS x, ST_Y(geom) AS y FROM osm.pois")) {
            System.out.println(resultSet);
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("properties");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                Point point = new Point(x, y, 4326);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode nameJson = objectMapper.readTree(name);
                Map<String, String> properties = new HashMap<>();
                Iterator<Map.Entry<String, JsonNode>> fields = nameJson.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    properties.put(field.getKey(), field.getValue().asText());
                }
                features.add(new Feature(id ,point, properties, "Point"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return features;
    }

    @Operation(tags = "Features", summary = "retrieve specific feature")
    @GetMapping(value = "/features/{name}", produces = "application/json")
    public HttpEntity<?> getFeatureByName(@PathVariable String name) {
        String connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";
        List<Feature> features = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(connectionString);
             PreparedStatement statement = connection.prepareStatement("SELECT id, properties, ST_X(geom) AS x, ST_Y(geom) AS y FROM osm.pois WHERE name = ?")) {

            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String properties1 = resultSet.getString("properties");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    Point point = new Point(x, y, 4326);

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode nameJson = objectMapper.readTree(properties1);
                    Map<String, String> properties = new HashMap<>();
                    Iterator<Map.Entry<String, JsonNode>> fields = nameJson.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        properties.put(field.getKey(), field.getValue().asText());
                    }
                    Feature feature = new Feature(id, point, properties, "Point");
                    features.add(feature);

//					return new HttpEntity<>(feature);
                }

            }
            if (features.isEmpty()) {
                return new ResponseEntity<>(String.format("No features with name %s exist on the Server.", name), HttpStatus.NOT_FOUND);
            } else {
                return new HttpEntity<>(features);
            }
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String CONNECTION_STRING = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";

    @Operation(tags = "Polygon", summary = "retrieve all features")
    @GetMapping(value = "/polygon", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Feature> getPolygon() {
        List<Feature> features = new ArrayList<>();
        String connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";

        try (Connection connection = DriverManager.getConnection(connectionString);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, properties, ST_AsText(geom) AS geom_wkt FROM osm.polygon")) {
            System.out.println(resultSet);
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String propertiesStr = resultSet.getString("properties");
                String geomWkt = resultSet.getString("geom_wkt");

                LineString lineString = parseWktToLineString(geomWkt);

                // Parse properties
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode propertiesJson = objectMapper.readTree(propertiesStr);
                Map<String, String> properties = new HashMap<>();
                Iterator<Map.Entry<String, JsonNode>> fields = propertiesJson.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    properties.put(field.getKey(), field.getValue().asText());
                }

                // Add feature to the list
                Feature feature = new Feature(id, lineString, properties, "Polygon");
                features.add(feature);
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return features;
    }

    @Operation(tags = "Hexagon", summary = "retrieve all features")
    @GetMapping(value = "/hexagon", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Feature> getHexagon() {
        List<Feature> features = new ArrayList<>();
        String connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";

        try (Connection connection = DriverManager.getConnection(connectionString);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, properties, ST_AsText(geom) AS geom_wkt FROM osm.hexagon")) {
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String propertiesStr = resultSet.getString("properties");
                String geomWkt = resultSet.getString("geom_wkt");

                LineString lineString = parseWktToLineString(geomWkt);
                // Parse properties
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode propertiesJson = objectMapper.readTree(propertiesStr);
                Map<String, String> properties = new HashMap<>();
                Iterator<Map.Entry<String, JsonNode>> fields = propertiesJson.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    properties.put(field.getKey(), field.getValue().asText());
                }

                // Add feature to the list
                Feature feature = new Feature(id, lineString, properties, "Polygon");
                features.add(feature);
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return features;
    }

    public static class PolygonRequest {
        @JsonProperty("coordinates")
        private List<List<Double>> coordinates;

        public List<List<Double>> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<List<Double>> coordinates) {
            this.coordinates = coordinates;
        }
    }

    @PostMapping(value="/intersection", produces = "application/json")
    public List<Feature> getFeaturesWithinPolygon(@RequestBody PolygonRequest polygonRequest) {
        List<Feature> features = new ArrayList<>();
        String connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";
        try (Connection connection = DriverManager.getConnection(connectionString);
             Statement statement = connection.createStatement()) {

            // Konvertiere die Koordinaten in WKT
            StringBuilder wktBuilder = new StringBuilder("POLYGON((");
            for (int i = 0; i < polygonRequest.getCoordinates().size(); i++) {
                List<Double> coord = polygonRequest.getCoordinates().get(i);
                wktBuilder.append(coord.get(0)).append(" ").append(coord.get(1));
                if (i < polygonRequest.getCoordinates().size() - 1) {
                    wktBuilder.append(", ");
                }
            }
            wktBuilder.append("))");

            String wktPolygon = wktBuilder.toString();
            System.out.println("WKT Polygon: " + wktPolygon);

            String query = String.format("SELECT id, properties, ST_X(geom) AS x, ST_Y(geom) AS y " +
                    "FROM osm.pois " +
                    "WHERE ST_Intersects(geom, ST_GeomFromText('%s', 4326))", wktPolygon);

            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("properties");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                System.out.println("[ " + x + ", " + y + "]");
                Point point = new Point(x, y, 4326);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode nameJson = objectMapper.readTree(name);
                Map<String, String> properties = new HashMap<>();
                Iterator<Map.Entry<String, JsonNode>> fields = nameJson.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    properties.put(field.getKey(), field.getValue().asText());
                }
                features.add(new Feature(id ,point, properties, "Point"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return features;
    }
    private LineString parseWktToLineString(String wkt) {
        // Remove "LINESTRING(" and ")" from the WKT string
        String coordinatesPart = wkt.substring("LINESTRING(".length(), wkt.length() - 1);
        String[] coords = coordinatesPart.split(",");
        List<Point> points = new ArrayList<>();
        for (String coord : coords) {
            String[] xy = coord.trim().split(" ");
            if (xy.length == 2) {
                double x = Double.parseDouble(xy[0]);
                double y = Double.parseDouble(xy[1]);
                points.add(new Point(x, y, 4326));  // Assuming 4326 as the SRID
            } else {
                System.err.println("Invalid coordinate pair: " + coord); // Debugging output for invalid pairs
            }
        }

        return new LineString(points, 4326);
    }
}
//		final var connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";
//		final var repository = new PostgresqlPoiRepository(connectionString);