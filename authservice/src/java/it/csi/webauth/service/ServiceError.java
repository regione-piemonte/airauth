/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.service;

public class ServiceError {

	enum Code {
		MISSING_PARAM, INVALID_PARAM, RESOURCE_NOT_FOUND, DATA_BASE_ERROR
	}

	private Code code;
	private String message;
	private String description;

	/**
	 * @param code
	 * @param message
	 * @param description
	 */
	public ServiceError(Code code, String message, String description) {
		this.code = code;
		this.message = message;
		this.description = description;
	}

	/**
	 * @param code
	 * @param message
	 */
	public ServiceError(Code code, String message) {
		this(code, message, (String) null);
	}

	/**
	 * @param code
	 * @param message
	 * @param throwable
	 */
	public ServiceError(Code code, String message, Throwable throwable) {
		this.code = code;
		this.message = message;
		this.description = throwable == null ? null : throwable.toString();
	}

	/**
	 * @param code
	 * @param throwable
	 */
	public ServiceError(Code code, Throwable throwable) {
		this(code, throwable.getMessage(), throwable.getCause());
	}

	/**
	 * @return
	 */
	public Code getCode() {
		return code;
	}

	/**
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

}
