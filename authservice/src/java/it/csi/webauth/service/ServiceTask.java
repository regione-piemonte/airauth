/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.service;

import it.csi.webauth.db.dao.DbAuth;
import it.csi.webauth.db.dao.DbAuthException;

import java.sql.SQLException;

interface ServiceTask {

	/**
	 * @param db
	 * @return
	 * @throws InvalidParamException
	 * @throws DbAuthException
	 * @throws SQLException
	 */
	Object execute(DbAuth db) throws InvalidParamException, DbAuthException,
			SQLException;

}
