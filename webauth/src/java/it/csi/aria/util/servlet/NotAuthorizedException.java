/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.util.servlet;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class NotAuthorizedException extends Exception {

	private static final long serialVersionUID = 2600014486241958462L;

	public NotAuthorizedException() {
	}

	/**
	 * @param message
	 */
	public NotAuthorizedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NotAuthorizedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

}
