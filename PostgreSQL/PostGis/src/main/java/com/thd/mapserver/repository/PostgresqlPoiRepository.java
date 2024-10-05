package com.thd.mapserver.repository;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

import com.thd.mapserver.domain.Feature;
import com.thd.mapserver.domain.PoiRepository;
import com.thd.mapserver.domain.geom.LineString;
import com.thd.mapserver.domain.geom.Point;

public class PostgresqlPoiRepository implements PoiRepository {
	private final String connectionString;

	public PostgresqlPoiRepository(String connectionString) {
		this.connectionString = connectionString;
	}

	@Override
	public void add(Feature poi, String name) {
		final var sqlUpdateStr = "INSERT INTO osm.pois VALUES (?, ?:: text, ?::jsonb, ST_GeomFromText(?, ?));";

		try (final var connection = DriverManager.getConnection(connectionString);
				var pstmt = connection.prepareStatement(sqlUpdateStr);) {
			final var point = (Point) poi.getGeometry();

			pstmt.setObject(1, UUID.fromString(poi.getId()));
			pstmt.setString(2, name);
			pstmt.setString(3, poi.getProperties().get("name"));
			final var wkt = String.format(Locale.ROOT, "POINT(%f %f)", point.getX(), point.getY());
			pstmt.setString(4, wkt);
			pstmt.setInt(5, point.srid());
			pstmt.executeUpdate();
		} catch (final SQLException e) {
			throw new PostgresqlException("Could not save the poi feature.", e);
		}
	}

	public void addpolygon(Feature poi, String name) {
		final var sqlUpdateStr = "INSERT INTO osm.polygon(id, name, properties, geom) VALUES (?, ?, ?::jsonb, ST_GeomFromText(?, ?));";

		try (final var connection = DriverManager.getConnection(connectionString);
			 var pstmt = connection.prepareStatement(sqlUpdateStr)) {

			pstmt.setObject(1, UUID.fromString(poi.getId()));
			pstmt.setString(2, name);
			pstmt.setString(3, poi.getProperties().get("name"));

			final var lineString = (LineString) poi.getGeometry();
			final var wkt = lineString.asText();
			final var srid = lineString.getSrid();

			pstmt.setString(4, wkt);
			pstmt.setInt(5, srid);
			pstmt.executeUpdate();
		} catch (final SQLException e) {
			throw new PostgresqlException("Could not save the linestring feature.", e);
		}
	}
	public void addhexagon(Feature poi, String name) {
		final var sqlUpdateStr = "INSERT INTO osm.hexagon(id, name, properties, geom) VALUES (?, ?, ?::jsonb, ST_GeomFromText(?, ?));";

		try (final var connection = DriverManager.getConnection(connectionString);
			 var pstmt = connection.prepareStatement(sqlUpdateStr)) {

			pstmt.setObject(1, UUID.fromString(poi.getId()));
			pstmt.setString(2, name);
			pstmt.setString(3, poi.getProperties().get("name"));

			final var lineString = (LineString) poi.getGeometry();
			final var wkt = lineString.asText();
			final var srid = lineString.getSrid();

			pstmt.setString(4, wkt);
			pstmt.setInt(5, srid);
			pstmt.executeUpdate();
		} catch (final SQLException e) {
			throw new PostgresqlException("Could not save the linestring feature.", e);
		}
	}

	// Alternative approach
	/*
	 * @Override public void add(Feature poi) { try (final var connection =
	 * DriverManager.getConnection(connectionString); final var statement =
	 * connection.createStatement();) { final var point = (Point) poi.getGeometry();
	 * final var insertStatement = String.format(Locale.ROOT,
	 * "INSERT INTO osm.pois VALUES ('%s', '%s', ST_GeomFromText('POINT(%f %f)', %d));"
	 * , poi.getId(), poi.getProperties().get("description"), point.getX(),
	 * point.getY(), point.getSrid());
	 *
	 * statement.executeUpdate(insertStatement); } catch (final SQLException e) {
	 * throw new PostgresqlException("Could not save the poi feature.", e); } }
	 */

}
