/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.service;

public class InvalidParamException extends Exception {

	private static final long serialVersionUID = 999068708673636362L;

	public InvalidParamException() {
	}

	/**
	 * @param message
	 */
	public InvalidParamException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidParamException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidParamException(String message, Throwable cause) {
		super(message, cause);
	}

}
