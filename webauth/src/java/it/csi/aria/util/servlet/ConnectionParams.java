/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.util.servlet;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class ConnectionParams {

	private String host;

	private int port;

	private String servicePath;

	private String user;

	private String password;

	public ConnectionParams(String host, int port, String servicePath,
			String user, String password) {
		this.host = host;
		this.port = port;
		this.servicePath = servicePath;
		this.user = user;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getServicePath() {
		return servicePath;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

}
