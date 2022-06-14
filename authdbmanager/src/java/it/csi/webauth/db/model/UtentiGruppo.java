/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

/**
 * 
 * 
 * @author alessio.calvio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */
public class UtentiGruppo extends Record {
	private Integer idUtente; // integer, not null default 'counter'
	private String utente; // character(32), not null
	private String nome; // character(32), not null
	private String cognome; // character(32), not null
	private String gruppo; // character(32), not null

	public void setIdUtente(Integer idUtente) {
		this.idUtente = idUtente;
	}

	public Integer getIdUtente() {
		return idUtente;
	}

	public void setUtente(String utente) {
		this.utente = utente;
	}

	public String getUtente() {
		return utente;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setGruppo(String gruppo) {
		this.gruppo = gruppo;
	}

	public String getGruppo() {
		return gruppo;
	}
}