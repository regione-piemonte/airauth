/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.util.servlet;

/**
 * Describe the purpose of this class
 * 
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = -1884054700497549179L;

	public ServiceException() {
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
