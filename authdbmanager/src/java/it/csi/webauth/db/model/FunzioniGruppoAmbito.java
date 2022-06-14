/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

public class FunzioniGruppoAmbito {
	private Integer idGruppo; // integer, not null
	private Integer idFunzione; // integer, not null
	private Integer idAmbito; // integer, not null

	public Integer getIdGruppo() {
		return idGruppo;
	}

	public void setIdGruppo(Integer idGruppo) {
		this.idGruppo = idGruppo;
	}

	public Integer getIdFunzione() {
		return idFunzione;
	}

	public void setIdFunzione(Integer idFunzione) {
		this.idFunzione = idFunzione;
	}

	public Integer getIdAmbito() {
		return idAmbito;
	}

	public void setIdAmbito(Integer idAmbito) {
		this.idAmbito = idAmbito;
	}
}
