package com.thd.mapserver.domain.geom;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class Geometry {
	protected final int srid;

	public Geometry(int srid) {
		this.srid = srid;
	}

	public int srid() {
		return this.srid;
	}

	public abstract String asText();

	public abstract String geometryType();

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (!(other instanceof Geometry)) {
			return false;
		}

		return srid == ((Geometry) other).srid;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(srid).toHashCode();
	}
}
