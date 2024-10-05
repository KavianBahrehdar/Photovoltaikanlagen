package com.thd.mapserver.domain.geom;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class Geometry {
	protected final int srid;

	public Geometry(int srid) {
		this.srid = srid;
	}

	public int SRID() {
		return this.srid;
	}

	public abstract String asText();

	public abstract String geometryType();

	public abstract int dimension();

	public abstract Envelope envelope();

	public abstract boolean isSimple();

	public abstract boolean equalsExact(Geometry other);

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (!(other instanceof Geometry)) {
			return false;
		}

		var otherGeom = (Geometry) other;
		return srid == otherGeom.srid && equalsExact(otherGeom);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(srid).toHashCode();
	}
}