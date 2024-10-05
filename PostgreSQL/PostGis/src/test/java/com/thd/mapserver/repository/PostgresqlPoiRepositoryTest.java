package com.thd.mapserver.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import com.thd.mapserver.domain.geom.LineString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.thd.mapserver.domain.Feature;
import com.thd.mapserver.domain.geom.Point;

public class PostgresqlPoiRepositoryTest {

	@Test
	public void add() throws SQLException, JSONException, IOException {
//		final var point = new Point(12, 48, 4326);
//		final var attributes = Map.of("name", "test point Kavian");
//		final var expectedId = UUID.randomUUID().toString();
//		final var feature = new Feature(expectedId, point, attributes);
		String jsonData = new String(Files.readAllBytes(Paths.get("src/main/resources/GeoJSON_with_ids.geojson")));
		JSONObject geoJson = new JSONObject(jsonData);
		JSONArray features = geoJson.getJSONArray("features");
		final var connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";
		final var repository = new PostgresqlPoiRepository(connectionString);
		for (int i = 0; i < features.length(); i++) {
			JSONObject feature = features.getJSONObject(i);
			JSONObject geometry = feature.getJSONObject("geometry");
			JSONObject properties = feature.getJSONObject("properties");
			String gemeinde = properties.optString("Gemeinde", "Unnamed");


			System.out.println(properties.toString());

//			String gemeinde = properties.optString("Gemeinde", "Unnamed");

			// Erstelle die Eigenschaften
			Map<String, String> attributes = new HashMap<>();

//			attributes.put("name", gemeinde);

			JSONArray coordinates = geometry.getJSONArray("coordinates");
			double x = coordinates.getDouble(0);
			double y = coordinates.getDouble(1);

			final var point = new Point(x, y, 4326);
			final var attribute = Map.of("name", String.valueOf(properties));
//			final var attributes = Map.of("name", properties);
			final var expectedId = UUID.randomUUID().toString();

			final var geoFeature = new Feature(expectedId, point, attribute);
			repository.add(geoFeature, gemeinde);
		}




//		try (final var connection = DriverManager.getConnection(connectionString);
//				final var statement = connection.createStatement();) {
//			final var resultSet = statement.executeQuery(
//					String.format("Select id, name, ST_AsText(geom) as geom from osm.pois where id='%s'", expectedId));
//
//			assertTrue(resultSet.next(), () -> "No pois for the given Id are stored in the database");
//
//			final var actualId = resultSet.getString("id");
//			final var actualDescription = resultSet.getString("name");
//			final var actualWkt = resultSet.getString("geom");
//
//			assertEquals(expectedId, actualId);
//			assertEquals("test point Kavian", actualDescription);
//			assertEquals("POINT(12 48)", actualWkt);
//		}
	}
	@Test
	public void addPolygon() throws SQLException, JSONException, IOException {
		String jsonData = new String(Files.readAllBytes(Paths.get("src/main/resources/DeggendorfLayerWGS84.geojson")));
		JSONObject geoJson = new JSONObject(jsonData);
		JSONArray features = geoJson.getJSONArray("features");
		final var connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";
		final var repository = new PostgresqlPoiRepository(connectionString);

		for (int i = 0; i < features.length(); i++) {
			JSONObject feature = features.getJSONObject(i);
			JSONObject geometry = feature.getJSONObject("geometry");
			JSONObject properties = feature.getJSONObject("properties");
			String gemeinde = properties.optString("GEN", "Unnamed");

			JSONArray coordinates = geometry.getJSONArray("coordinates");
			List<Point> points = new ArrayList<>();

			for (int j = 0; j < coordinates.length(); j++) {
				JSONArray polygon = coordinates.getJSONArray(j);
				for (int k = 0; k < polygon.length(); k++) {
					JSONArray ring = polygon.getJSONArray(k);
					for (int l = 0; l < ring.length(); l++) {
						JSONArray point = ring.getJSONArray(l);
						double x = point.getDouble(0);
						double y = point.getDouble(1);
						points.add(new Point(x, y, 4326));
					}
				}
			}

			final var lineString = new LineString(points, 4326);
			final var attribute = Map.of("name", String.valueOf(properties));
			final var expectedId = UUID.randomUUID().toString();

			final var geoFeature = new Feature(expectedId, lineString, attribute);
			repository.addpolygon(geoFeature, gemeinde);
		}
	}
	@Test
	public void addHexagon() throws SQLException, JSONException, IOException {
		String jsonData = new String(Files.readAllBytes(Paths.get("src/main/resources/DeggendorfHexagon.geojson")));
		JSONObject geoJson = new JSONObject(jsonData);
		JSONArray features = geoJson.getJSONArray("features");
		final var connectionString = "jdbc:postgresql://localhost:5432/Geoinformatik2?currentSchema=osm&user=postgres&password=@Kavianbrd78";
		final var repository = new PostgresqlPoiRepository(connectionString);

		for (int i = 0; i < features.length(); i++) {
			JSONObject feature = features.getJSONObject(i);
			JSONObject geometry = feature.getJSONObject("geometry");
			JSONObject properties = feature.getJSONObject("properties");

			JSONArray coordinates = geometry.getJSONArray("coordinates");
			List<Point> points = new ArrayList<>();

			for (int j = 0; j < coordinates.length(); j++) {
				JSONArray polygon = coordinates.getJSONArray(j);
				for (int k = 0; k < polygon.length(); k++) {
					JSONArray ring = polygon.getJSONArray(k);
					for (int l = 0; l < ring.length(); l++) {
						JSONArray point = ring.getJSONArray(l);
						double x = point.getDouble(0);
						double y = point.getDouble(1);
						points.add(new Point(x, y, 4326));
					}
				}
			}

			final var lineString = new LineString(points, 4326);
			final var attribute = Map.of("name", String.valueOf(properties));
			final var expectedId = UUID.randomUUID().toString();

			final var geoFeature = new Feature(expectedId, lineString, attribute);
			repository.addhexagon(geoFeature, "Deggendorf");
		}
	}
}
