/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.dao;

public class DbAuthNotFoundException extends DbAuthException {

	private static final long serialVersionUID = 6232745589016346955L;

	public DbAuthNotFoundException() {
	}

	public DbAuthNotFoundException(String description) {
		super(description);
	}

}
