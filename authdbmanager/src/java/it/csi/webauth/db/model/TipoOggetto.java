/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

import java.sql.Timestamp;

public class TipoOggetto extends Record {
	private Integer idTipoOggetto; // integer, not null default 'counter'
	private String descrizione; // string, not null
	private Timestamp dataAgg; // timestamp with t.z., not null default 'now'
	private Integer autoreAgg; // integer, not null

	public Integer getIdTipoOggetto() {
		return idTipoOggetto;
	}

	public void setIdTipoOggetto(Integer idTipoOggetto) {
		this.idTipoOggetto = idTipoOggetto;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
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
