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

public class GruppoUtente extends Record {
	private Integer idUtente; // integer, not null
	private Integer idGruppo; // integer, not null
	private Timestamp dataAgg; // timestamp with t.z., not null default 'now'
	private Integer autoreAgg; // integer, not null

	public void setIdUtente(Integer idUtente) {
		this.idUtente = idUtente;
	}

	public Integer getIdUtente() {
		return idUtente;
	}

	public void setIdGruppo(Integer idGruppo) {
		this.idGruppo = idGruppo;
	}

	public Integer getIdGruppo() {
		return idGruppo;
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
