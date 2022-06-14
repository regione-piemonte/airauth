/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.util.servlet;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class SessionExpiredException extends Exception {

	private static final long serialVersionUID = 280869829508853909L;

	public SessionExpiredException() {
	}

	/**
	 * @param message
	 */
	public SessionExpiredException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SessionExpiredException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SessionExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

}
