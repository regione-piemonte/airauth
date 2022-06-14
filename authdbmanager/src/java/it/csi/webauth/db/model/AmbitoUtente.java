/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

import java.sql.Timestamp;

public class AmbitoUtente extends Record {
	private Integer idAmbito; // integer, not null
	private Integer idUtente; // integer, not null
	private Timestamp dataAgg; // timestamp with t.z., not null default 'now'
	private Integer autoreAgg; // integer, not null

	public Integer getIdAmbito() {
		return idAmbito;
	}

	public void setIdAmbito(Integer idAmbito) {
		this.idAmbito = idAmbito;
	}

	public Integer getIdUtente() {
		return idUtente;
	}

	public void setIdUtente(Integer idUtente) {
		this.idUtente = idUtente;
	}

	public Timestamp getDataAgg() {
		return dataAgg;
	}

	public void setDataAgg(Timestamp dataAgg) {
		this.dataAgg = dataAgg;
	}

	public Integer getAutoreAgg() {
		return autoreAgg;
	}

	public void setAutoreAgg(Integer autoreAgg) {
		this.autoreAgg = autoreAgg;
	}

}
