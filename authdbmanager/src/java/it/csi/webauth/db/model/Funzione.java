/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

import java.sql.Timestamp;

/**
 * 
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */

public class Funzione extends Record {
	private Integer idFunzione;// integer, not null default 'counter'
	private String funzione; // character varying(32), not null
	private String descrizione; // character varying(128)
	private Timestamp dataAgg; // timestamp with t.z., not null default 'now'
	private Integer autoreAgg; // integer, not null

	public void setIdFunzione(Integer idFunzione) {
		this.idFunzione = idFunzione;
	}

	public Integer getIdFunzione() {
		return idFunzione;
	}

	public void setFunzione(String funzione) {
		this.funzione = funzione;
	}

	public String getFunzione() {
		return funzione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDataAgg(Timestamp dataAgg) {
		this.dataAgg = dataAgg;
	}

	public Timestamp getDataAgg() {
		return dataAgg;
	}

	public void setAutoreAgg(Integer autoreAgg) {
		this.autoreAgg = autoreAgg;
	}

	public Integer getAutoreAgg() {
		return autoreAgg;
	}
}
