/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

import java.sql.Timestamp;

/**
 * 
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * @version $Revision: 1.1 $
 */

public class AmbitoAcl extends Record {
	private Integer idAmbito; // integer, not null default 'counter'
	private Integer idTipoOggetto; // integer, not null
	private String idOggetto; // character varying(32), not null
	private Timestamp dataAgg; // timestamp with t.z., not null default 'now'
	private Integer autoreAgg; // integer, not null

	public void setIdAmbito(Integer idAmbito) {
		this.idAmbito = idAmbito;
	}

	public Integer getIdAmbito() {
		return idAmbito;
	}

	public void setIdTipoOggetto(Integer tipoOggetto) {
		this.idTipoOggetto = tipoOggetto;
	}

	public Integer getIdTipoOggetto() {
		return idTipoOggetto;
	}

	public void setIdOggetto(String idOggetto) {
		this.idOggetto = idOggetto;
	}

	public String getIdOggetto() {
		return idOggetto;
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
