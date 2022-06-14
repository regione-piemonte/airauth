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

public class Gruppo extends Record {
	private Integer idGruppo; // integer, not null default 'counter'
	private String gruppo; // character varying(32), not null
	private String descrizione; // character varying(128)
	private Timestamp dataAgg; // timestamp with t.z., not null default 'now'
	private Integer autoreAgg; // integer, not null

	public void setIdGruppo(Integer idGruppo) {
		this.idGruppo = idGruppo;
	}

	public Integer getIdGruppo() {
		return idGruppo;
	}

	public void setGruppo(String gruppo) {
		this.gruppo = gruppo;
	}

	public String getGruppo() {
		return gruppo;
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
