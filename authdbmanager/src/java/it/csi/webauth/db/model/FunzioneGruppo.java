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

public class FunzioneGruppo extends Record {
	private Integer idGruppo; // integer, not null
	private Integer idFunzione; // integer, not null
	//private Integer idAmbito; // integer, not null default 0
	private Boolean fnScrittura;// boolean, not null
	private Boolean fnAvanzata; // boolean, not null
	private Timestamp dataAgg; // timestamp with t.z., not null default 'now'
	private Integer autoreAgg; // integer, not null

	public void setIdGruppo(Integer idGruppo) {
		this.idGruppo = idGruppo;
	}

	public Integer getIdGruppo() {
		return idGruppo;
	}

	public void setIdFunzione(Integer idFunzione) {
		this.idFunzione = idFunzione;
	}

	public Integer getIdFunzione() {
		return idFunzione;
	}

	/*public void setIdAmbito(Integer idAmbito) {
		this.idAmbito = idAmbito;
	}

	public Integer getIdAmbito() {
		return idAmbito;
	}*/

	public void setFnScrittura(Boolean fnScrittura) {
		this.fnScrittura = fnScrittura;
	}

	public Boolean getFnScrittura() {
		return fnScrittura;
	}

	public void setFnAvanzata(Boolean fnAvanzata) {
		this.fnAvanzata = fnAvanzata;
	}

	public Boolean getFnAvanzata() {
		return fnAvanzata;
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
