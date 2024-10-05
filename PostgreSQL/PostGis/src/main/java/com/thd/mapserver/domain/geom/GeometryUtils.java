package com.thd.mapserver.domain.geom;

import java.util.List;
import java.util.Locale;

public final class GeometryUtils {

	private GeometryUtils() {
	}

	public static String convertCoordinatesToWkt(Point point) {
		return Double.isNaN(point.getZ()) ? String.format(Locale.US, "%.7f %.7f", point.getX(), point.getY())
				: String.format(Locale.US, "%.7f %.7f %.7f", point.getX(), point.getY(), point.getZ());
	}

	public static boolean isSimple(List<Point> vertices) {
        return false;
    }
}
