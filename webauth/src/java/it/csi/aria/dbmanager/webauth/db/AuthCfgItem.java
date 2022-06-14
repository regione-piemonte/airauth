/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */

package it.csi.aria.dbmanager.webauth.db;

import it.csi.util.config.Parameter;
import it.csi.util.config.ParametricConfigItem;
import it.csi.webauth.db.dao.DbConnParams;
import it.csi.webauth.db.dao.DbToolkit;

/**
 * Classe per la configurazione del servlet di gestione della banca dati per
 * l'autenticazione.
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.3 $, $Date: 2013/05/13 07:29:18 $
 */
public class AuthCfgItem extends ParametricConfigItem {
	private static final long serialVersionUID = -7164489030133540379L;
	public static final int descrizione = 0;
	public static final int indirizzo_host = 1;
	public static final int porta_host = 2;
	public static final int data_base = 3;
	public static final int utente = 4;
	public static final int password = 5;
	public static final int data_base_engine = 6;
	public static final int encrypt_cmd = 7;

	private static final Parameter parameters[] = {
			new Parameter("descrizione", TYPE_STRING),
			new Parameter("indirizzo host", TYPE_STRING),
			new Parameter("porta host", TYPE_INTEGER, new Integer(0)),
			new Parameter("data base", TYPE_STRING),
			new Parameter("utente", TYPE_STRING),
			new Parameter("password", TYPE_STRING),
			new Parameter("data base engine", TYPE_STRING, "postgres"),
			new Parameter("comando crittografia", TYPE_STRING) };

	public AuthCfgItem() {
		super(parameters);
	}

	public DbConnParams getDbConnParams() {
		String tipo_db = (String) getValue(data_base_engine);
		int protocol;
		if (tipo_db.equalsIgnoreCase("postgres"))
			protocol = DbToolkit.DB_POSTGRES;
		else if (tipo_db.equalsIgnoreCase("oracle"))
			protocol = DbToolkit.DB_ORACLE;
		else
			protocol = DbToolkit.DB_UNKNOWN;
		DbConnParams dbConnParams = new DbConnParams(
				(String) getValue(indirizzo_host),
				((Integer) getValue(porta_host)).intValue(), protocol,
				(String) getValue(data_base), (String) getValue(utente),
				(String) getValue(password));
		return (dbConnParams);
	}

}
