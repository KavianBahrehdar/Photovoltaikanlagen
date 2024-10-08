package com.thd.mapserver.domain.geom;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Point extends Geometry {
	private static final String TYPENAME_POINT = "Point";
	private static final double NULL_ORDINATE = Double.NaN;
	private static final double NULL_MEASURE = Double.NaN;
	private static final double EPSILON = 1E-7;

	private final double x;
	private final double y;
	private final double z;
	private final double m;

	public Point(double x, double y, int srid) {
		this(x, y, NULL_ORDINATE, NULL_MEASURE, srid);
	}

	public Point(double x, double y, double z, int srid) {
		this(x, y, z, NULL_MEASURE, srid);
	}

	public Point(double x, double y, double z, double m, int srid) {
		super(srid);
		this.x = x;
		this.y = y;
		this.z = z;
		this.m = m;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public double getM(){ return this.m; }


	@Override
	public boolean equalsExact(Geometry other) {
		if (!(other instanceof Point)) {
			return false;
		}

		final Point otherPoint = (Point) other;
		return Math.abs(x - otherPoint.x) < EPSILON && Math.abs(y - otherPoint.y) < EPSILON
				&& (Math.abs(z - otherPoint.z) < EPSILON || Double.isNaN(z) && Double.isNaN(otherPoint.z));
	}

	@Override
	public boolean equals(final Object other) {
		if (other == this) {
			return true;
		}

		if (!(other instanceof Point)) {
			return false;
		}

		final Point otherPoint = (Point) other;
		return super.equals(other) && Math.abs(x - otherPoint.x) < EPSILON && Math.abs(y - otherPoint.y) < EPSILON
				&& (Math.abs(z - otherPoint.z) < EPSILON || Double.isNaN(z) && Double.isNaN(otherPoint.z));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(x).append(y).append(z).toHashCode();
	}

	@Override
	public String asText() {
		return String.format("%s(%s)", TYPENAME_POINT.toUpperCase(), GeometryUtils.convertCoordinatesToWkt(this));
	}

	@Override
	public String geometryType() {
		return TYPENAME_POINT;
	}

	@Override
	public int dimension() {
		return 0;
	}

	@Override
	public Envelope envelope() {
		return new Envelope(this.x, this.y, this.x, this.y);
	}

	@Override
	public boolean isSimple() {
		return true;
	}

}

