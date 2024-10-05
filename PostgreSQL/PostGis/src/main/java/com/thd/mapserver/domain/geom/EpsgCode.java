package com.thd.mapserver.domain.geom;

public enum EpsgCode {
	WGS84(4326);

	private int code;

	EpsgCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}

}
