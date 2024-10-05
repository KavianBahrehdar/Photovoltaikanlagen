package com.thd.mapserver.repository;

public class PostgresqlException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PostgresqlException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
