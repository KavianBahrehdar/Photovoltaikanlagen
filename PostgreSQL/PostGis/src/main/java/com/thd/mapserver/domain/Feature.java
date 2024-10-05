package com.thd.mapserver.domain;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.thd.mapserver.domain.geom.Geometry;

public class Feature {

	private final String id;
	private final Geometry geometry;
	private final Map<String, String> properties;
	private final String featureType;

	public Feature(String id, Geometry geometry, Map<String, String> properties) {
		this(id, geometry, properties, "");
	}

	public Feature(String id, Geometry geometry, String featureType) {
		this(id, geometry, Collections.emptyMap(), featureType);
	}

	public Feature(String id, Geometry geometry, Map<String, String> properties, String featureType) {
		this.id = id;
		this.geometry = geometry;
		this.properties = properties;
		this.featureType = featureType;
	}

	public String getId() {
		return this.id;
	}

	public Geometry getGeometry() {
		return this.geometry;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public String getFeatureType() {
		return this.featureType;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (!(other instanceof Feature)) {
			return false;
		}

		final var otherFeature = (Feature) other;
		return id.equals(otherFeature.getId()) && geometry.equals(otherFeature.getGeometry())
				&& properties.equals(otherFeature.getProperties())
				&& StringUtils.equals(featureType, otherFeature.getFeatureType());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(id).append(geometry).append(properties).append(featureType)
				.toHashCode();
	}

}