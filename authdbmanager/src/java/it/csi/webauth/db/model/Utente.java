/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * 
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */

public class Utente extends Record {
	private Integer idUtente; // integer, not null default counter
	private String utente; // character varying(32), not null
	private String password; // character varying(40), not null
	private Boolean abilitazione;// boolean, not null
	private Date dataScadenza; // date
	private String nome; // character varying(32)
	private String cognome; // character varying(32)
	private String azienda; // character varying(32)
	private String mail; // character varying(64)
	private String telefono; // character varying(32)
	private String indirizzo; // character varying(64)
	private Timestamp dataAgg; // timestamp with t.z., not null default now
	private Integer autoreAgg; // integer, not null

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

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setAbilitazione(Boolean abilitazione) {
		this.abilitazione = abilitazione;
	}

	public Boolean getAbilitazione() {
		return abilitazione;
	}

	public void setDataScadenza(Date dataScadenza) {
		this.dataScadenza = dataScadenza;
	}

	public Date getDataScadenza() {
		return dataScadenza;
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

	public void setAzienda(String azienda) {
		this.azienda = azienda;
	}

	public String getAzienda() {
		return azienda;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	public String getIndirizzo() {
		return indirizzo;
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
