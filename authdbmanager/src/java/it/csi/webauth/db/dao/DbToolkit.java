/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.dao;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Classe di utilita' per l'interfacciamento ai data base
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */
public class DbToolkit {
	// Versione
	public static final String VERSION = "$Revision: 1.1 $";

	// data base engine
	public static final int DB_POSTGRES = 0;
	public static final int DB_ORACLE = 1;
	private static final int DB_NUMBER = 2; // mantenere aggiornato !!!
	public static final int DB_UNKNOWN = DB_NUMBER;

	private static DbToolkit dbt = null;
	private boolean[] registeredDb;

	// private int loginTimeout = 30;

	// Si impedisce l'istanziazione di DbToolkit al di fuori di questa classe
	private DbToolkit() {
		registeredDb = new boolean[DB_NUMBER];
		for (int i = 0; i < registeredDb.length; i++)
			registeredDb[i] = false;
		// DriverManager.setLogWriter(new java.io.PrintWriter(System.err));
	}

	public synchronized static DbToolkit getDbToolkit() {
		if (dbt == null)
			dbt = new DbToolkit();
		return (dbt);
	}

	public synchronized void registerJDBCDriver(int vendor)
			throws DbAuthException, SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		String driverMainClass;

		switch (vendor) {
		case DB_POSTGRES:
			driverMainClass = "org.postgresql.Driver";
			break;
		case DB_ORACLE:
			driverMainClass = "oracle.jdbc.driver.OracleDriver";
			break;
		default:
			throw new DbAuthException("Driver JDBC non supportato");
		}
		if (!registeredDb[vendor]) {
			Class<?> drvClass = Class.forName(driverMainClass);
			Enumeration<?> enumeration = DriverManager.getDrivers();
			while (enumeration.hasMoreElements()) {
				Object registeredDriver = enumeration.nextElement();
				if (drvClass.isInstance(registeredDriver)) {
					registeredDb[vendor] = true;
					return;
				}
			}
			Driver drv = (Driver) (drvClass.newInstance());
			DriverManager.registerDriver(drv);
			registeredDb[vendor] = true;
		}
		return;
	}

	public Connection openConnection(DbConnParams params)
			throws DbAuthException, SQLException, IOException {
		String dbURL;

		switch (params.getEngine()) {
		case DB_POSTGRES:
			if (params.getPort() > 0)
				dbURL = "jdbc:postgresql://" + params.getHost() + ":"
						+ params.getPort() + "/" + params.getDbName();
			else
				// usa porta di default
				dbURL = "jdbc:postgresql://" + params.getHost() + "/"
						+ params.getDbName();
			break;
		case DB_ORACLE:
			dbURL = "jdbc:oracle:thin:@" + params.getHost() + ":"
					+ params.getPort() + ":" + params.getDbName();
			break;
		default:
			throw new DbAuthException("Data Base non supportato");
		}
		if (!registeredDb[params.getEngine()])
			throw new DbAuthException();
		// DriverManager.setLoginTimeout(loginTimeout);
		if (params.getPort() > 0) {
			Socket testConn = new Socket(params.getHost(), params.getPort());
			testConn.close();
		}

		Connection conn = DriverManager.getConnection(dbURL,
				params.getDbUser(), params.getDbPassword());
		return (conn);
	}

	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}

	/*
	 * public int getLoginTimeout() { return(loginTimeout); }
	 * 
	 * public boolean setLoginTimeout(int seconds) { if (seconds <= 1)
	 * return(false);
	 * 
	 * loginTimeout = seconds; return(true); }
	 */
}
