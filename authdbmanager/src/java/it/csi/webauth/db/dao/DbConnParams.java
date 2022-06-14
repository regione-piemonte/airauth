/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.dao;

/**
 * Classe base per contenere i parametri di connessione ad un data base.
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */
public class DbConnParams {
	// Versione
	public static final String VERSION = "$Revision: 1.1 $";

	private String host;
	private int port;
	private int engine;
	private String dbName;
	private String dbUser;
	private String dbPassword;

	public DbConnParams(String host, int port, int engine, String dbName,
			String dbUser, String dbPassword) {
		this.host = host;
		this.port = port;
		this.engine = engine;
		this.dbName = dbName;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getEngine() {
		return engine;
	}

	public String getDbName() {
		return dbName;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

}
