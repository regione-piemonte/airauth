/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.dao;

import it.csi.webauth.db.model.Ambito;
import it.csi.webauth.db.model.AmbitoAcl;
import it.csi.webauth.db.model.AmbitoUtente;
import it.csi.webauth.db.model.FunctionFlags;
import it.csi.webauth.db.model.Funzione;
import it.csi.webauth.db.model.FunzioneGruppo;
import it.csi.webauth.db.model.FunzioniGruppoAmbito;
import it.csi.webauth.db.model.Gruppo;
import it.csi.webauth.db.model.GruppoUtente;
import it.csi.webauth.db.model.TipoOggetto;
import it.csi.webauth.db.model.Utente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import sun.misc.BASE64Encoder;

/**
 * 
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.2 $
 */

public class DbAuth {
	public static final String REVISION = "$Revision: 1.2 $";

	private static String encryptCmdLine = "openssl passwd -crypt";

	private static String verifyEncryptCmdLine = "openssl passwd -crypt -salt";

	private String name;

	private DbConnParams connParams = null;

	private DataSource dataSource = null;

	private Connection connection = null;

	private boolean writeEnabled;

	private static Logger logger = Logger.getLogger("webauth.db."
			+ DbAuth.class.getSimpleName());

	public DbAuth(String name, DbConnParams connParams) {
		this(name, connParams, false);
	}

	public DbAuth(String name, DataSource dataSource) {
		this(name, dataSource, false);
	}

	public DbAuth(String name, DbConnParams connParams, boolean writeEnabled) {
		this(name, writeEnabled);
		if (connParams == null)
			throw new IllegalArgumentException(
					"DbConnParams argument should not be null");
		this.connParams = connParams;
	}

	public DbAuth(String name, DataSource dataSource, boolean writeEnabled) {
		this(name, writeEnabled);
		if (dataSource == null)
			throw new IllegalArgumentException(
					"DataSource argument should not be null");
		this.dataSource = dataSource;
	}

	/**
	 * @param name
	 * @param writeEnabled
	 */
	private DbAuth(String name, boolean writeEnabled) {
		PropertyConfigurator.configure(getClass().getResource(
				"/log4j.properties"));
		this.name = name;
		this.writeEnabled = writeEnabled;
	}

	/**
	 * @throws DbAuthException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void connect() throws DbAuthException, SQLException, IOException {
		System.out.println("1");
		if (dataSource == null) {
			System.out.println("2");
			try {
				System.out.println("3");
				DbToolkit.getDbToolkit().registerJDBCDriver(
						DbToolkit.DB_POSTGRES);
				System.out.println("4");
			} catch (Exception ex) {
				System.out.println("5");
				throw new DbAuthException(
						"Errore in registrazione driver JDBC: " + ex);
			}
			System.out.println("6");
			connection = DbToolkit.getDbToolkit().openConnection(connParams);
			System.out.println("7");
		} else {
			System.out.println("8");
			connection = dataSource.getConnection();
		}
		System.out.println("9");
		
	}

	/**
	 * diconnect
	 * @throws SQLException
	 */
	public void disconnect() throws SQLException {
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	public String getName() {
		return (name);
	}

	// ---------------------------- Table utenti ------------------------------
	/**
	 * @param userKey
	 * @return
	 * @throws SQLException
	 */
	public List<Utente> readUserList(String userKey) // List of Utente
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = null;
		if (userKey != null)
			userKey = userKey.trim();
		if (userKey == null || userKey.length() == 0)
			query = "select id_utente, utente, nome, cognome, azienda, data_scadenza from utenti order by cognome, nome, utente";
		else {
			query = "select id_utente, utente, nome, cognome, azienda, data_scadenza from utenti where ("
					+ "utente ilike '"
					+ userKey
					+ "%' or "
					+ "cognome ilike '"
					+ userKey
					+ "%' or "
					+ "nome ilike '"
					+ userKey
					+ "%' or "
					+ "((cognome is null or nome is null) and "
					+ " utente ilike '%"
					+ userKey
					+ "%')) "
					+ "order by cognome, nome, utente";
		}
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Utente> list = new ArrayList<Utente>();
		while (rs.next()) {
			Utente utente = new Utente();
			utente.setIdUtente(rsGetInt(rs, 1));
			utente.setUtente(rsGetString(rs, 2));
			utente.setNome(rsGetString(rs, 3));
			utente.setCognome(rsGetString(rs, 4));
			utente.setAzienda(rsGetString(rs, 5));
			utente.setDataScadenza(rsGetDate(rs, 6));
			list.add(utente);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param utente
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Integer readUserId(String utente) throws SQLException,
			DbAuthException {
		if (utente == null)
			throw new IllegalArgumentException("null argument: utente");

		Statement stmt = connection.createStatement();
		String query = "select id_utente from utenti where utente="
				+ qVal(utente);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Integer id_utente = null;
		if (rs.next()) {
			id_utente = rsGetInt(rs, 1);
		}
		rs.close();
		stmt.close();
		if (id_utente == null)
			throw new DbAuthNotFoundException("record for user " + utente
					+ " not found");
		return (id_utente);
	}

	/**
	 * @param utente
	 * @return
	 * @throws SQLException
	 */
	public boolean checkUser(String utente) throws SQLException {
		if (utente == null)
			throw new IllegalArgumentException("null argument: utente");

		Statement stmt = connection.createStatement();
		String query = "select id_utente from utenti where utente="
				+ qVal(utente);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		boolean exists = rs.next();
		rs.close();
		stmt.close();
		return (exists);
	}

	/**
	 * @param id_utente
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Utente readUser(Integer id_utente) throws SQLException,
			DbAuthException {
		if (id_utente == null)
			throw new IllegalArgumentException("null argument: id_utente");

		Statement stmt = connection.createStatement();
		String query = "select utente, password, abilitazione, data_scadenza, "
				+ "nome, cognome, azienda, mail, telefono, indirizzo, data_agg, "
				+ "autore_agg from utenti where id_utente=" + qVal(id_utente);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Utente utente = null;
		if (rs.next()) {
			utente = new Utente();
			utente.setIdUtente(id_utente);
			utente.setUtente(rsGetString(rs, rs.findColumn("utente")));
			utente.setPassword(rsGetString(rs, rs.findColumn("password")));
			utente.setAbilitazione(rsGetBoolean(rs,
					rs.findColumn("abilitazione")));
			utente.setDataScadenza(rsGetDate(rs, rs.findColumn("data_Scadenza")));
			utente.setNome(rsGetString(rs, rs.findColumn("nome")));
			utente.setCognome(rsGetString(rs, rs.findColumn("cognome")));
			utente.setAzienda(rsGetString(rs, rs.findColumn("azienda")));
			utente.setMail(rsGetString(rs, rs.findColumn("mail")));
			utente.setTelefono(rsGetString(rs, rs.findColumn("telefono")));
			utente.setIndirizzo(rsGetString(rs, rs.findColumn("indirizzo")));
			utente.setDataAgg(rsGetTimestamp(rs, rs.findColumn("data_agg")));
			utente.setAutoreAgg(rsGetInt(rs, rs.findColumn("autore_agg")));
		}
		rs.close();
		stmt.close();
		if (utente == null)
			throw new DbAuthNotFoundException("record for user " + id_utente
					+ " not found");
		return (utente);
	}

	/**
	 * @param id_utente
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public String readUsername(Integer id_utente) throws SQLException,
			DbAuthException {
		if (id_utente == null)
			throw new IllegalArgumentException("null argument: id_utente");

		Statement stmt = connection.createStatement();
		String query = "select utente from utenti where id_utente="
				+ qVal(id_utente);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		String username = null;
		if (rs.next()) {
			username = rsGetString(rs, 1);
		}
		rs.close();
		stmt.close();
		if (username == null)
			throw new DbAuthNotFoundException("record for user " + id_utente
					+ " not found");
		return (username);
	}

	/*
	 * private Integer readUserDomainId(Integer id_utente) throws SQLException,
	 * DbAuthException { if (id_utente == null) throw new
	 * IllegalArgumentException("null argument: id_utente");
	 * 
	 * Statement stmt = connection.createStatement(); String query =
	 * "select id_ambito from ambiti_utenti where id_utente=" + qVal(id_utente);
	 * logger.debug(query); ResultSet rs = stmt.executeQuery(query); Integer
	 * id_ambito = null; if (rs.next()) { id_ambito = rsGetInt(rs, 1); }
	 * rs.close(); stmt.close(); if (id_ambito == null) throw new
	 * NotFoundException("record for user " + id_utente + " not found"); return
	 * (id_ambito); }
	 */

	/**
	 * @param utente
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertUser(Utente utente) throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (utente == null)
			throw new IllegalArgumentException("null argument: utente");

		Statement stmt = connection.createStatement();
		String ins = "insert into utenti (utente, password, abilitazione, data_scadenza, nome, cognome, azienda, mail, telefono, indirizzo, autore_agg) values ("
				+ qVal(utente.getUtente())
				+ ", "
				+ qVal(utente.getPassword())
				+ ", "
				+ qVal(utente.getAbilitazione())
				+ ", "
				+ qVal(utente.getDataScadenza())
				+ ", "
				+ qVal(utente.getNome())
				+ ", "
				+ qVal(utente.getCognome())
				+ ", "
				+ qVal(utente.getAzienda())
				+ ", "
				+ qVal(utente.getMail())
				+ ", "
				+ qVal(utente.getTelefono())
				+ ", "
				+ qVal(utente.getIndirizzo())
				+ ", "
				+ qVal(utente.getAutoreAgg()) + ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param utente
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void updateUser(Utente utente) throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (utente == null)
			throw new IllegalArgumentException("null argument: utente");

		Statement stmt = connection.createStatement();
		String upd = "update utenti set "
				+ (utente.getPassword() == null ? "" : "password="
						+ qVal(utente.getPassword()) + ", ") + "abilitazione="
				+ qVal(utente.getAbilitazione()) + ", " + "data_scadenza="
				+ qVal(utente.getDataScadenza()) + ", " + "nome="
				+ qVal(utente.getNome()) + ", " + "cognome="
				+ qVal(utente.getCognome()) + ", " + "azienda="
				+ qVal(utente.getAzienda()) + ", " + "mail="
				+ qVal(utente.getMail()) + ", " + "telefono="
				+ qVal(utente.getTelefono()) + ", " + "indirizzo="
				+ qVal(utente.getIndirizzo()) + ", "
				+ "data_agg=current_timestamp(0), " + "autore_agg="
				+ qVal(utente.getAutoreAgg()) + " " + "where id_utente="
				+ qVal(utente.getIdUtente());
		logger.debug(upd);
		int count = stmt.executeUpdate(upd);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("update count = " + count
					+ ", expected 1");
		return;
	}

	public void deleteUser(Integer id_utente) throws SQLException,
			DbAuthException {
		if (!writeEnabled) {
			logger.error("DbAuth is not write enabled");
			throw new DbAuthException("DbAuth is not write enabled");
		}
		if (id_utente == null) {
			logger.error("null argument: id_utente");
			throw new IllegalArgumentException("null argument: id_utente");
		}

		Statement stmt = connection.createStatement();
		String del = "delete from utenti where id_utente=" + qVal(id_utente);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param expiryDate
	 * @return
	 * @throws SQLException
	 */
	public List<Utente> readExpiredUserList(java.util.Date expiryDate)
			throws SQLException {
		Date expirySqlDate = expiryDate == null ? null : new Date(
				expiryDate.getTime());
		Statement stmt = connection.createStatement();
		String query = "select id_utente,utente,nome,cognome,data_scadenza,azienda from"
				+ " utenti where data_scadenza < " + qVal(expirySqlDate);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Utente> lista_utenti = new ArrayList<Utente>();
		while (rs.next()) {
			// id_utente utente nome cognome gruppo
			Utente ug = new Utente();
			ug.setIdUtente(rsGetInt(rs, 1));
			ug.setUtente(rsGetString(rs, 2));
			ug.setNome(rsGetString(rs, 3));
			ug.setCognome(rsGetString(rs, 4));
			ug.setDataScadenza(rsGetDate(rs, 5));
			ug.setAzienda(rsGetString(rs, 6));
			lista_utenti.add(ug);
		}
		rs.close();
		stmt.close();
		return (lista_utenti);
	}

	// ---------------------------- Table gruppi ------------------------------
	/**
	 * @return
	 * @throws SQLException
	 */
	public List<Gruppo> readGroupList() // List of Gruppo
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_gruppo, gruppo, descrizione from gruppi order by gruppo";
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Gruppo> list = new ArrayList<Gruppo>();
		while (rs.next()) {
			Gruppo gruppo = new Gruppo();
			gruppo.setIdGruppo(rsGetInt(rs, 1));
			gruppo.setGruppo(rsGetString(rs, 2));
			gruppo.setDescrizione(rsGetString(rs, 3));
			list.add(gruppo);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param gruppo
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Integer readGroupId(String gruppo) throws SQLException,
			DbAuthException {
		Statement stmt = connection.createStatement();
		String query = "select id_gruppo from gruppi where gruppo = "
				+ qVal(gruppo);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Integer id_gruppo = null;
		if (rs.next()) {
			id_gruppo = rsGetInt(rs, 1);
		}
		rs.close();
		stmt.close();
		if (id_gruppo == null)
			throw new DbAuthNotFoundException("record for group " + gruppo
					+ " not found");
		return id_gruppo;
	}

	/**
	 * @param id_gruppo
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Gruppo readGroup(Integer id_gruppo) throws SQLException,
			DbAuthException {
		Statement stmt = connection.createStatement();
		String query = "select id_gruppo, gruppo, descrizione, data_agg, autore_agg from gruppi where id_gruppo="
				+ qVal(id_gruppo);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Gruppo gruppo = null;
		if (rs.next()) {
			gruppo = new Gruppo();
			gruppo.setIdGruppo(rsGetInt(rs, 1));
			gruppo.setGruppo(rsGetString(rs, 2));
			gruppo.setDescrizione(rsGetString(rs, 3));
			gruppo.setDataAgg(rsGetTimestamp(rs, 4));
			gruppo.setAutoreAgg(rsGetInt(rs, 5));
		}
		rs.close();
		stmt.close();
		if (gruppo == null)
			throw new DbAuthNotFoundException("record for group " + id_gruppo
					+ " not found");
		return (gruppo);
	}

	/**
	 * @param gruppo
	 * @return
	 * @throws SQLException
	 */
	public boolean checkGroup(String gruppo) throws SQLException {
		if (gruppo == null)
			throw new IllegalArgumentException("null argument: gruppo");

		Statement stmt = connection.createStatement();
		String query = "select id_gruppo from gruppi where gruppo="
				+ qVal(gruppo);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		boolean exists = rs.next();
		rs.close();
		stmt.close();
		return (exists);
	}

	/**
	 * @param gruppo
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertGroup(Gruppo gruppo) throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (gruppo == null)
			throw new IllegalArgumentException("null argument: gruppo");

		Statement stmt = connection.createStatement();
		String ins = "insert into gruppi (gruppo, descrizione, autore_agg) values ("
				+ qVal(gruppo.getGruppo())
				+ ", "
				+ qVal(gruppo.getDescrizione())
				+ ", "
				+ qVal(gruppo.getAutoreAgg()) + ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param gruppo
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void updateGroup(Gruppo gruppo) throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (gruppo == null)
			throw new IllegalArgumentException("null argument: gruppo");

		Statement stmt = connection.createStatement();
		String upd = "update gruppi set " + "descrizione="
				+ qVal(gruppo.getDescrizione()) + ", "
				+ "data_agg=current_timestamp(0), " + "autore_agg="
				+ qVal(gruppo.getAutoreAgg()) + " " + "where id_gruppo="
				+ qVal(gruppo.getIdGruppo());
		logger.debug(upd);
		int count = stmt.executeUpdate(upd);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("update count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param id_gruppo
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteGroup(Integer id_gruppo) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (id_gruppo == null)
			throw new IllegalArgumentException("null argument: id_gruppo");

		Statement stmt = connection.createStatement();
		String del = "delete from gruppi where id_gruppo=" + qVal(id_gruppo);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	// ---------------------------- Table funzioni ----------------------------
	/**
	 * @return
	 * @throws SQLException
	 */
	public List<Funzione> readFunctionList() // List of Funzione
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_funzione, funzione, descrizione from funzioni order by funzione";
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Funzione> list = new ArrayList<Funzione>();
		while (rs.next()) {
			Funzione funzione = new Funzione();
			funzione.setIdFunzione(rsGetInt(rs, 1));
			funzione.setFunzione(rsGetString(rs, 2));
			funzione.setDescrizione(rsGetString(rs, 3));
			list.add(funzione);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param funzione
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Integer readFunctionId(String funzione) throws SQLException,
			DbAuthException {
		if (funzione == null)
			throw new IllegalArgumentException("null argument: funzione");

		Statement stmt = connection.createStatement();
		String query = "select id_funzione from funzioni where funzione="
				+ qVal(funzione);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Integer id_funzione = null;
		if (rs.next()) {
			id_funzione = rsGetInt(rs, 1);
		}
		rs.close();
		stmt.close();
		if (id_funzione == null)
			throw new DbAuthNotFoundException("record for funzione " + funzione
					+ " not found");
		return (id_funzione);
	}

	// ---------------------------- Table tipo_oggetto
	// ----------------------------
	/**
	 * @return
	 * @throws SQLException
	 */
	public List<TipoOggetto> readTypeObjectList() // List of TipoOggetto
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_tipo_oggetto, descrizione from tipo_oggetto order by descrizione";
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<TipoOggetto> list = new ArrayList<TipoOggetto>();
		while (rs.next()) {
			TipoOggetto tipoOggetto = new TipoOggetto();
			tipoOggetto.setIdTipoOggetto(rsGetInt(rs, 1));
			tipoOggetto.setDescrizione(rsGetString(rs, 2));
			list.add(tipoOggetto);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param tipoOggetto
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Integer readTypeObjectId(String tipoOggetto) throws SQLException,
			DbAuthException {
		if (tipoOggetto == null)
			throw new IllegalArgumentException("null argument: tipoOggetto");

		Statement stmt = connection.createStatement();
		String query = "select id_tipo_oggetto from tipo_oggetto where descrizione="
				+ qVal(tipoOggetto);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Integer id_tipo_oggetto = null;
		if (rs.next()) {
			id_tipo_oggetto = rsGetInt(rs, 1);
		}
		rs.close();
		stmt.close();
		if (id_tipo_oggetto == null)
			throw new DbAuthNotFoundException("record for tipoOggetto " + tipoOggetto
					+ " not found");
		return (id_tipo_oggetto);
	}

	/**
	 * @param idTipoOggetto
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public String readTypeObjectDesc(Integer idTipoOggetto)
			throws SQLException, DbAuthException {
		if (idTipoOggetto == null)
			throw new IllegalArgumentException("null argument: idTipoOggetto");

		Statement stmt = connection.createStatement();
		String query = "select descrizione from tipo_oggetto where id_tipo_oggetto="
				+ qVal(idTipoOggetto);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		String descrizione = null;
		if (rs.next()) {
			descrizione = rsGetString(rs, 1);
		}
		rs.close();
		stmt.close();
		if (descrizione == null)
			throw new DbAuthNotFoundException("record for idTipoOggetto "
					+ idTipoOggetto + " not found");
		return (descrizione);
	}

	// ---------------------------- Table ambiti ------------------------------
	/**
	 * @return
	 * @throws SQLException
	 */
	public List<Ambito> readDomainList() // List of Ambito
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_ambito, ambito, descrizione from ambiti";
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Ambito> list = new ArrayList<Ambito>();
		while (rs.next()) {
			Ambito ambito = new Ambito();
			ambito.setIdAmbito(rsGetInt(rs, 1));
			ambito.setAmbito(rsGetString(rs, 2));
			ambito.setDescrizione(rsGetString(rs, 3));
			list.add(ambito);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param ambito
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Integer readDomainId(String ambito) throws SQLException,
			DbAuthException {
		if (ambito == null)
			throw new IllegalArgumentException("null argument: ambito");

		Statement stmt = connection.createStatement();
		String query = "select id_ambito from ambiti where ambito="
				+ qVal(ambito);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Integer id_ambito = null;
		if (rs.next()) {
			id_ambito = rsGetInt(rs, 1);
		}
		rs.close();
		stmt.close();
		if (id_ambito == null)
			throw new DbAuthNotFoundException("record for ambito " + ambito
					+ " not found");
		return (id_ambito);
	}

	/**
	 * @param id_ambito
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Ambito readDomain(Integer id_ambito) throws SQLException,
			DbAuthException {
		Statement stmt = connection.createStatement();
		String query = "select ambito, descrizione, data_agg, autore_agg from ambiti where id_ambito="
				+ qVal(id_ambito);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		Ambito ambito = null;
		if (rs.next()) {
			ambito = new Ambito();
			ambito.setIdAmbito(id_ambito);
			ambito.setAmbito(rsGetString(rs, 1));
			ambito.setDescrizione(rsGetString(rs, 2));
			ambito.setDataAgg(rsGetTimestamp(rs, 3));
			ambito.setAutoreAgg(rsGetInt(rs, 4));
		}
		rs.close();
		stmt.close();
		if (ambito == null)
			throw new DbAuthNotFoundException("record for domain " + id_ambito
					+ " not found");
		return (ambito);
	}

	/**
	 * @param ambito
	 * @return
	 * @throws SQLException
	 */
	public boolean checkDomain(String ambito) throws SQLException {
		if (ambito == null)
			throw new IllegalArgumentException("null argument: ambito");

		Statement stmt = connection.createStatement();
		String query = "select id_ambito from ambiti where ambito="
				+ qVal(ambito);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		boolean exists = rs.next();
		rs.close();
		stmt.close();
		return (exists);
	}

	/**
	 * @param ambito
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertDomain(Ambito ambito) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (ambito == null)
			throw new IllegalArgumentException("null argument: ambito");

		Statement stmt = connection.createStatement();
		String ins = "insert into ambiti (ambito, descrizione, autore_agg) values ("
				+ qVal(ambito.getAmbito())
				+ ", "
				+ qVal(ambito.getDescrizione())
				+ ", "
				+ qVal(ambito.getAutoreAgg()) + ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param ambito
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void updateDomain(Ambito ambito) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (ambito == null)
			throw new IllegalArgumentException("null argument: ambito");

		Statement stmt = connection.createStatement();
		String upd = "update ambiti set " + "descrizione="
				+ qVal(ambito.getDescrizione()) + ", "
				+ "data_agg=current_timestamp(0), " + "autore_agg="
				+ qVal(ambito.getAutoreAgg()) + "where id_ambito="
				+ qVal(ambito.getIdAmbito());
		logger.debug(upd);
		int count = stmt.executeUpdate(upd);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("update count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param id_ambito
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteDomain(Integer id_ambito) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (id_ambito == null)
			throw new IllegalArgumentException("null argument: id_ambito");

		Statement stmt = connection.createStatement();
		String del = "delete from ambiti where id_ambito=" + qVal(id_ambito);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	// ---------------------------- Table ambiti_acl
	// ------------------------------
	/**
	 * @return
	 * @throws SQLException
	 */
	public List<AmbitoAcl> readDomainAclList() throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_ambito, id_tipo_oggetto, id_oggetto from ambiti_acl";
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<AmbitoAcl> list = new ArrayList<AmbitoAcl>();
		while (rs.next()) {
			AmbitoAcl ambitoAcl = new AmbitoAcl();
			ambitoAcl.setIdAmbito(rsGetInt(rs, 1));
			ambitoAcl.setIdTipoOggetto(rsGetInt(rs, 2));
			ambitoAcl.setIdOggetto(rsGetString(rs, 3));
			list.add(ambitoAcl);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param objectType
	 * @return
	 * @throws SQLException
	 */
	public List<AmbitoAcl> readDomainAclListFromObjectType(Integer objectType)
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "SELECT id_ambito, id_oggetto FROM ambiti_acl WHERE "
				+ "id_tipo_oggetto = " + objectType;
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<AmbitoAcl> list = new ArrayList<AmbitoAcl>();
		while (rs.next()) {
			AmbitoAcl ambitoAcl = new AmbitoAcl();
			ambitoAcl.setIdAmbito(rsGetInt(rs, rs.findColumn("id_ambito")));
			ambitoAcl.setIdTipoOggetto(objectType);
			ambitoAcl
					.setIdOggetto(rsGetString(rs, rs.findColumn("id_oggetto")));
			list.add(ambitoAcl);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param id_ambito
	 * @param tipo_oggetto
	 * @return
	 * @throws SQLException
	 */
	public List<AmbitoAcl> readDomainAcl(Integer id_ambito, Integer tipo_oggetto)
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_oggetto, data_agg, autore_agg from ambiti_acl where id_ambito="
				+ qVal(id_ambito)
				+ " and id_tipo_oggetto="
				+ qVal(tipo_oggetto);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<AmbitoAcl> ambitiAclList = new ArrayList<AmbitoAcl>();
		while (rs.next()) {
			AmbitoAcl ambitoAcl = new AmbitoAcl();
			ambitoAcl.setIdAmbito(id_ambito);
			ambitoAcl.setIdTipoOggetto(tipo_oggetto);
			ambitoAcl.setIdOggetto(rsGetString(rs, 1));
			ambitoAcl.setDataAgg(rsGetTimestamp(rs, 2));
			ambitoAcl.setAutoreAgg(rsGetInt(rs, 3));
			ambitiAclList.add(ambitoAcl);
		}
		rs.close();
		stmt.close();
		return ambitiAclList;
	}

	/**
	 * @param ambitoAcl
	 * @return
	 * @throws SQLException
	 */
	public boolean checkDomainAcl(AmbitoAcl ambitoAcl) throws SQLException {
		if (ambitoAcl == null)
			throw new IllegalArgumentException("null argument: ambitoAcl");

		Statement stmt = connection.createStatement();
		String query = "select id_ambito from ambiti_acl where id_ambito="
				+ qVal(ambitoAcl.getIdAmbito()) + " and id_tipo_oggetto="
				+ qVal(ambitoAcl.getIdTipoOggetto()) + " and id_oggetto="
				+ qVal(ambitoAcl.getIdOggetto());
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		boolean exists = rs.next();
		rs.close();
		stmt.close();
		return (exists);
	}

	/**
	 * @param ambitoAcl
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertDomainAcl(AmbitoAcl ambitoAcl) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (ambitoAcl == null)
			throw new IllegalArgumentException("null argument: ambitoAcl");

		Statement stmt = connection.createStatement();
		String ins = "insert into ambiti_acl (id_ambito, id_tipo_oggetto, id_oggetto, autore_agg) values ("
				+ qVal(ambitoAcl.getIdAmbito())
				+ ", "
				+ qVal(ambitoAcl.getIdTipoOggetto())
				+ ", "
				+ qVal(ambitoAcl.getIdOggetto())
				+ ", "
				+ qVal(ambitoAcl.getAutoreAgg()) + ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param ambitoAcl
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteDomainAcl(AmbitoAcl ambitoAcl) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (ambitoAcl == null)
			throw new IllegalArgumentException("null argument: ambitoAcl");

		Statement stmt = connection.createStatement();
		String del = "delete from ambiti_acl where id_ambito="
				+ qVal(ambitoAcl.getIdAmbito()) + " and id_tipo_oggetto="
				+ qVal(ambitoAcl.getIdTipoOggetto()) + " and id_oggetto="
				+ qVal(ambitoAcl.getIdOggetto());
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	// ---------------------------- Table ambiti_utenti -----------------------
	/**
	 * @param idAmbito
	 * @return
	 * @throws SQLException
	 */
	public List<Utente> readUserListForDomain(Integer idAmbito)
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select utenti.id_utente,utenti.utente,utenti.nome,utenti.cognome "
				+ "from utenti ,ambiti_utenti where ambiti_utenti.id_utente=utenti.id_utente and  "
				+ "ambiti_utenti.id_ambito=" + qVal(idAmbito);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Utente> lista_utenti = new ArrayList<Utente>();
		while (rs.next()) {
			// id_utente utente nome cognome gruppo
			Utente ug = new Utente();
			ug.setIdUtente(rsGetInt(rs, 1));
			ug.setUtente(rsGetString(rs, 2));
			ug.setNome(rsGetString(rs, 3));
			ug.setCognome(rsGetString(rs, 4));
			lista_utenti.add(ug);
		}
		rs.close();
		stmt.close();
		return (lista_utenti);
	}

	/**
	 * @param id_utente
	 * @return
	 * @throws SQLException
	 */
	public List<Integer> readUserDomain(Integer id_utente) // List of Integer
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_ambito from ambiti_utenti where id_utente="
				+ qVal(id_utente);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Integer> list = new ArrayList<Integer>();
		while (rs.next()) {
			list.add(rsGetInt(rs, 1));
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param id_utente
	 * @param id_ambito
	 * @return
	 * @throws SQLException
	 */
	public boolean isUserInDomain(Integer id_utente, Integer id_ambito)
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_utente from ambiti_utenti where id_utente="
				+ qVal(id_utente) + " and id_ambito=" + qVal(id_ambito);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		boolean result = rs.next();
		rs.close();
		stmt.close();
		return (result);
	}

	/**
	 * @param ambitoUtente
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertUserDomain(AmbitoUtente ambitoUtente)
			throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (ambitoUtente == null || ambitoUtente.getIdUtente() == null
				|| ambitoUtente.getIdAmbito() == null
				|| ambitoUtente.getAutoreAgg() == null)
			throw new IllegalArgumentException("null argument");

		Statement stmt = connection.createStatement();
		String ins = "insert into ambiti_utenti (id_utente, id_ambito, autore_agg) values ("
				+ qVal(ambitoUtente.getIdUtente())
				+ ", "
				+ qVal(ambitoUtente.getIdAmbito())
				+ ", "
				+ qVal(ambitoUtente.getAutoreAgg()) + ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param ambitoUtente
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteUserDomain(AmbitoUtente ambitoUtente)
			throws SQLException, DbAuthException {
		if (ambitoUtente == null)
			throw new IllegalArgumentException("null argument");
		deleteUserDomain(ambitoUtente.getIdUtente(), ambitoUtente.getIdAmbito());
	}

	/**
	 * @param idUtente
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteUserFromDomain(Integer idUtente) throws SQLException,
			DbAuthException {
		if (idUtente == null)
			throw new IllegalArgumentException("null argument: id_utente");
		Statement stmt = connection.createStatement();
		String del = "delete from ambiti_utenti where id_utente="
				+ qVal(idUtente);
		logger.debug(del);
		stmt.executeUpdate(del);
		stmt.close();
		return;
	}

	/**
	 * @param idAmbito
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteDomainFromUserDomain(Integer idAmbito)
			throws SQLException, DbAuthException {
		if (idAmbito == null)
			throw new IllegalArgumentException("null argument: id_ambito");
		Statement stmt = connection.createStatement();
		String del = "delete from ambiti_utenti where id_ambito="
				+ qVal(idAmbito);
		logger.debug(del);
		stmt.executeUpdate(del);
		stmt.close();
		return;
	}

	/**
	 * @param id_utente
	 * @param id_ambito
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteUserDomain(Integer id_utente, Integer id_ambito)
			throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (id_utente == null)
			throw new IllegalArgumentException("null argument: id_utente");
		if (id_ambito == null)
			throw new IllegalArgumentException("null argument: id_gruppo");

		Statement stmt = connection.createStatement();
		String del = "delete from ambiti_utenti where id_utente="
				+ qVal(id_utente) + " and id_ambito=" + qVal(id_ambito);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	// ---------------------------- Table gruppi_utente -----------------------
	/**
	 * @param groupId
	 * @return
	 * @throws SQLException
	 */
	public List<Utente> readUserListForGroup(Integer groupId)
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select utenti.id_utente,utenti.utente,utenti.nome,utenti.cognome "
				+ "from utenti ,gruppi_utente where gruppi_utente.id_utente=utenti.id_utente and  "
				+ "gruppi_utente.id_gruppo=" + qVal(groupId);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Utente> lista_utenti = new ArrayList<Utente>();
		while (rs.next()) {
			// id_utente utente nome cognome gruppo
			Utente ug = new Utente();
			ug.setIdUtente(rsGetInt(rs, 1));
			ug.setUtente(rsGetString(rs, 2));
			ug.setNome(rsGetString(rs, 3));
			ug.setCognome(rsGetString(rs, 4));
			lista_utenti.add(ug);
		}
		rs.close();
		stmt.close();
		return (lista_utenti);
	}

	/**
	 * @param id_utente
	 * @return
	 * @throws SQLException
	 */
	public List<Integer> readUserGroups(Integer id_utente) // List of Integer
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_gruppo from gruppi_utente where id_utente="
				+ qVal(id_utente);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Integer> list = new ArrayList<Integer>();
		while (rs.next()) {
			list.add(rsGetInt(rs, 1));
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param id_utente
	 * @param id_gruppo
	 * @return
	 * @throws SQLException
	 */
	public boolean isUserInGroup(Integer id_utente, Integer id_gruppo)
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_utente from gruppi_utente where id_utente="
				+ qVal(id_utente) + " and id_gruppo=" + qVal(id_gruppo);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		boolean result = rs.next();
		rs.close();
		stmt.close();
		return (result);
	}

	/*
	 * public boolean isUserInGroup(String utente, String gruppo) throws
	 * SQLException { Statement stmt = connection.createStatement(); String
	 * query = "select utente from gruppi_utente where utente=" + qVal(utente) +
	 * " and gruppo=" + qVal(gruppo); logger.debug(query); ResultSet rs =
	 * stmt.executeQuery(query); boolean result = rs.next(); rs.close();
	 * stmt.close(); return (result); }
	 */

	/**
	 * @param gu
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertUserGroup(GruppoUtente gu) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (gu == null || gu.getIdUtente() == null || gu.getIdGruppo() == null
				|| gu.getAutoreAgg() == null)
			throw new IllegalArgumentException("null argument");

		Statement stmt = connection.createStatement();
		String ins = "insert into gruppi_utente (id_utente, id_gruppo, autore_agg) values ("
				+ qVal(gu.getIdUtente())
				+ ", "
				+ qVal(gu.getIdGruppo())
				+ ", "
				+ qVal(gu.getAutoreAgg()) + ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param gu
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteUserGroup(GruppoUtente gu) throws SQLException,
			DbAuthException {
		if (gu == null)
			throw new IllegalArgumentException("null argument");
		deleteUserGroup(gu.getIdUtente(), gu.getIdGruppo());
	}

	/**
	 * @param id_utente
	 * @param id_gruppo
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteUserGroup(Integer id_utente, Integer id_gruppo)
			throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (id_utente == null)
			throw new IllegalArgumentException("null argument: id_utente");
		if (id_gruppo == null)
			throw new IllegalArgumentException("null argument: id_gruppo");

		Statement stmt = connection.createStatement();
		String del = "delete from gruppi_utente where id_utente="
				+ qVal(id_utente) + " and id_gruppo=" + qVal(id_gruppo);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	// ---------------------------- Table funzioni_gruppo ---------------------
	/**
	 * @param id_gruppo
	 * @return
	 * @throws SQLException
	 */
	public List<FunzioneGruppo> readGroupFunctions(Integer id_gruppo) // List of
			// FunzioneGruppo
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_funzione, fn_scrittura, fn_avanzata,"
				+ " data_agg, autore_agg from funzioni_gruppo where id_gruppo="
				+ qVal(id_gruppo) + " order by id_funzione";
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<FunzioneGruppo> list = new ArrayList<FunzioneGruppo>();
		while (rs.next()) {
			FunzioneGruppo fg = new FunzioneGruppo();
			fg.setIdGruppo(id_gruppo);
			fg.setIdFunzione(rsGetInt(rs, rs.findColumn("id_funzione")));
			fg.setFnScrittura(rsGetBoolean(rs, rs.findColumn("fn_scrittura")));
			fg.setFnAvanzata(rsGetBoolean(rs, rs.findColumn("fn_avanzata")));
			fg.setDataAgg(rsGetTimestamp(rs, rs.findColumn("data_agg")));
			fg.setAutoreAgg(rsGetInt(rs, rs.findColumn("autore_agg")));
			list.add(fg);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/*
	 * public HashMap<Integer, List<Integer>> getFunctionDomainMapFromGroup(
	 * Integer groupId) {
	 * 
	 * return null; }
	 */

	/**
	 * @param fgruppo
	 * @return
	 * @throws SQLException
	 */
	public List<FunzioniGruppoAmbito> readGroupFunctionsDomains(
			FunzioneGruppo fgruppo) // List of
			// FunzioneGruppo
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "select id_ambito from funzioni_gruppi_ambiti where id_gruppo = "
				+ qVal(fgruppo.getIdGruppo())
				+ " and id_funzione = "
				+ qVal(fgruppo.getIdFunzione());
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<FunzioniGruppoAmbito> list = new ArrayList<FunzioniGruppoAmbito>();
		while (rs.next()) {
			FunzioniGruppoAmbito fg = new FunzioniGruppoAmbito();
			fg.setIdGruppo(fgruppo.getIdGruppo());
			fg.setIdFunzione(fgruppo.getIdFunzione());
			fg.setIdAmbito(rs.getInt(rs.findColumn("id_ambito")));
			list.add(fg);
		}
		rs.close();
		stmt.close();
		return (list);
	}

	/**
	 * @param fg
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertGroupFunction(FunzioneGruppo fg) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (fg == null || fg.getIdGruppo() == null
				|| fg.getIdFunzione() == null || fg.getFnScrittura() == null
				|| fg.getFnAvanzata() == null || fg.getAutoreAgg() == null)
			throw new IllegalArgumentException("null argument");

		Statement stmt = connection.createStatement();
		String ins = "insert into funzioni_gruppo (id_gruppo, id_funzione, fn_scrittura, fn_avanzata, autore_agg) values ("
				+ qVal(fg.getIdGruppo())
				+ ", "
				+ qVal(fg.getIdFunzione())
				+ ", "
				+ qVal(fg.getFnScrittura())
				+ ", "
				+ qVal(fg.getFnAvanzata())
				+ ", "
				+ qVal(fg.getAutoreAgg())
				+ ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param fga
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void insertGroupFunctionDomain(FunzioniGruppoAmbito fga)
			throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (fga == null || fga.getIdGruppo() == null
				|| fga.getIdFunzione() == null || fga.getIdAmbito() == null)
			throw new IllegalArgumentException("null argument");

		Statement stmt = connection.createStatement();
		String ins = "INSERT INTO funzioni_gruppi_ambiti (id_gruppo, "
				+ "id_funzione, id_ambito) VALUES (" + qVal(fga.getIdGruppo())
				+ ", " + qVal(fga.getIdFunzione()) + ", "
				+ qVal(fga.getIdAmbito()) + ")";
		logger.debug(ins);
		int count = stmt.executeUpdate(ins);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("insert count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param fg
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void updateGroupFunction(FunzioneGruppo fg) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (fg == null || fg.getIdGruppo() == null
				|| fg.getIdFunzione() == null || fg.getFnScrittura() == null
				|| fg.getFnAvanzata() == null || fg.getAutoreAgg() == null)
			throw new IllegalArgumentException("null argument");

		Statement stmt = connection.createStatement();
		String upd = "UPDATE funzioni_gruppo SET " + "fn_scrittura = "
				+ qVal(fg.getFnScrittura()) + ", " + "fn_avanzata = "
				+ qVal(fg.getFnAvanzata()) + ", "
				+ "data_agg = current_timestamp(0), " + "autore_agg = "
				+ qVal(fg.getAutoreAgg()) + " " + "WHERE id_gruppo = "
				+ qVal(fg.getIdGruppo()) + " AND " + "id_funzione = "
				+ qVal(fg.getIdFunzione());
		logger.debug(upd);
		int count = stmt.executeUpdate(upd);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("update count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param fg
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteGroupFunction(FunzioneGruppo fg) throws SQLException,
			DbAuthException {
		if (fg == null)
			throw new IllegalArgumentException("null argument");
		deleteGroupFunction(fg.getIdGruppo(), fg.getIdFunzione());
	}

	public void deleteGroupFunction(Integer id_gruppo, Integer id_funzione)
			throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (id_gruppo == null)
			throw new IllegalArgumentException("null argument: id_gruppo");
		if (id_funzione == null)
			throw new IllegalArgumentException("null argument: id_funzione");

		Statement stmt = connection.createStatement();
		String del = "delete from funzioni_gruppo where id_gruppo="
				+ qVal(id_gruppo) + " and id_funzione=" + qVal(id_funzione);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	/**
	 * @param id_funzione
	 * @param id_gruppo
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteDomainFromGroupFunction(Integer id_funzione,
			Integer id_gruppo) throws SQLException, DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (id_gruppo == null)
			throw new IllegalArgumentException("null argument: id_gruppo");
		if (id_funzione == null)
			throw new IllegalArgumentException("null argument: id_funzione");

		Statement stmt = connection.createStatement();
		String del = "DELETE FROM funzioni_gruppi_ambiti WHERE id_gruppo="
				+ qVal(id_gruppo) + " AND id_funzione=" + qVal(id_funzione);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		logger.debug("Cancellati " + count + " ambiti associati");
		stmt.close();
		return;
	}

	/**
	 * @param fgd
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteGroupFunctionDomain(FunzioniGruppoAmbito fgd)
			throws SQLException, DbAuthException {
		if (fgd == null)
			throw new IllegalArgumentException("null argument");
		deleteGroupFunctionDomain(fgd.getIdGruppo(), fgd.getIdFunzione(),
				fgd.getIdAmbito());
	}

	/**
	 * @param id_gruppo
	 * @param id_funzione
	 * @param id_ambito
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public void deleteGroupFunctionDomain(Integer id_gruppo,
			Integer id_funzione, Integer id_ambito) throws SQLException,
			DbAuthException {
		if (!writeEnabled)
			throw new DbAuthException("DbAuth is not write enabled");
		if (id_gruppo == null)
			throw new IllegalArgumentException("null argument: id_gruppo");
		if (id_funzione == null)
			throw new IllegalArgumentException("null argument: id_funzione");
		if (id_ambito == null)
			throw new IllegalArgumentException("null argument: id_ambito");

		Statement stmt = connection.createStatement();
		String del = "delete from funzioni_gruppi_ambiti where id_gruppo="
				+ qVal(id_gruppo) + " and id_funzione=" + qVal(id_funzione)
				+ " and id_ambito=" + qVal(id_ambito);
		logger.debug(del);
		int count = stmt.executeUpdate(del);
		stmt.close();
		if (count != 1)
			throw new DbAuthException("delete count = " + count
					+ ", expected 1");
		return;
	}

	// ---------------------------- More Tables ------------------------------
	/*
	 * Ritorna, dati il nome di un utente e di una funzione, una Map che ha,
	 * come chiavi, gli ambiti su cui l'utente ha tale funzione e, come valori,
	 * i flag associati alla funzione per ciascun ambito.
	 */
	/**
	 * @param user
	 * @param function
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public Map<Integer, FunctionFlags> getPermissionMap(String user,
			String function) throws SQLException, DbAuthException {
		Integer userId = readUserId(user);
		Integer functionId = readFunctionId(function);

		Map<Integer, FunctionFlags> permissionMap = new HashMap<Integer, FunctionFlags>();
		// leggo i gruppi a cui appartiene l'utente
		List<Integer> userGroups = readUserGroups(userId);
		logger.debug("L'utente ha " + userGroups.size() + " gruppi");
		Iterator<Integer> it_gr = userGroups.iterator();
		while (it_gr.hasNext()) {
			// per ogni gruppo a cui appartiene verifico se possiede la funzione
			// specificata
			Integer groupId = it_gr.next();
			logger.debug("Considero il gruppo " + groupId + " "
					+ readGroup(groupId).getGruppo());
			Statement stmt = connection.createStatement();
			String query = "select fn_scrittura, fn_avanzata from funzioni_gruppo where id_gruppo="
					+ qVal(groupId) + " and id_funzione=" + qVal(functionId);
			logger.debug(query);
			ResultSet rs = stmt.executeQuery(query);
			FunzioneGruppo fg = null;
			if (rs.next()) {
				fg = new FunzioneGruppo();
				fg.setIdGruppo(groupId);
				fg.setIdFunzione(functionId);
				fg.setFnScrittura(rs.getBoolean(rs.findColumn("fn_scrittura")));
				fg.setFnAvanzata(rs.getBoolean(rs.findColumn("fn_avanzata")));
				logger.debug("fg:");
				logger.debug("	idgruppo: " + groupId);
				logger.debug("	functionId: " + functionId);
				logger.debug("	fg write " + fg.getFnScrittura());
				logger.debug("	fg avanzata " + fg.getFnAvanzata());
			}
			rs.close();

			if (fg != null) {
				boolean foundFga = false;
				logger.debug("l'utente possiede la funzione " + functionId
						+ " con il gruppo " + groupId);
				// se esite la funzione associata al gruppo di appartenenza
				// verifico se ci sono degli ambiti nella tabella
				// funzioni_gruppi_ambiti
				logger.debug("Verifico se c'e' l'ambito associato alla funzione-gruppo");
				query = "SELECT id_ambito FROM funzioni_gruppi_ambiti"
						+ " WHERE id_funzione = " + qVal(functionId)
						+ " AND id_gruppo = " + qVal(groupId);
				logger.debug(query);
				rs = stmt.executeQuery(query);

				while (rs.next()) {
					// se ci sono gruppi ambiti funzioni
					// carico la mappa
					int idAmbito = rs.getInt(rs.findColumn("id_ambito"));
					logger.debug("trovato l'ambito " + idAmbito + " "
							+ readDomain(idAmbito).getAmbito());
					FunctionFlags ff = permissionMap.get(idAmbito);
					if (ff == null) {
						ff = new FunctionFlags();
						permissionMap.put(idAmbito, ff);
						logger.debug("Inserito nella permission map l'ambito: "
								+ idAmbito);
					}
					ff.orWriteFlag(fg.getFnScrittura());
					ff.orAdvancedFlag(fg.getFnAvanzata());
					logger.debug(" inserisco nella mappa i flag writeflag: "
							+ ff.getWriteFlag() + " e advancedflag: "
							+ ff.getAdvancedFlag());
					foundFga = true;
				}
				rs.close();

				if (!foundFga) {
					// se non ci sono gruppi ambiti funzioni
					logger.debug("Non ci sono ambiti_gruppi_funzuioni associati al gruppo "
							+ fg.getIdGruppo());
					// se non ho trovato nessuna corrispondenza con gli
					// ambiti del gruppo verifico se l'utente possiede degli
					// ambiti
					// personali e gli associo i flag persenti in fg
					List<Integer> userDomainList = readUserDomain(userId);
					logger.debug("L'utente " + userId + " possiede "
							+ userDomainList.size() + " ambiti personali");
					if (userDomainList.size() == 0) {
						// l'utente non ha nessun ambito personale associato
						logger.debug("L'utente " + userId
								+ " non ha nessun ambito personale associato.");
					} else {
						for (int i = 0; i < userDomainList.size(); i++) {
							int idUserDomain = userDomainList.get(i);
							FunctionFlags ff = permissionMap.get(idUserDomain);
							logger.debug("Trovato ambito personale "
									+ idUserDomain + " "
									+ readDomain(idUserDomain).getAmbito());
							if (ff == null) {
								ff = new FunctionFlags();
								permissionMap.put(idUserDomain, ff);
								logger.debug("Inserito nella permission map l'ambito: "
										+ idUserDomain);
							}
							ff.orWriteFlag(fg.getFnScrittura());
							ff.orAdvancedFlag(fg.getFnAvanzata());
							logger.debug(" inserisco nella mappa i flag writeflag: "
									+ ff.getWriteFlag()
									+ " e advancedflag: "
									+ ff.getAdvancedFlag());

						}// end for

					}// end else
				}// end if
			} else {
				logger.info("l'utente non possiede funzione " + functionId
						+ " e gruppo " + groupId);
			}

			stmt.close();
		}// end per ogni gruppo{
		logger.debug("Stampo la permissionMap ottenuta per l'utente " + user
				+ " e per la funzione " + function);
		Iterator<Integer> it_perm = permissionMap.keySet().iterator();
		while (it_perm.hasNext()) {
			Integer idAmbito = it_perm.next();
			logger.debug("chiave ambito " + idAmbito + " "
					+ readDomain(idAmbito).getAmbito());
			FunctionFlags ff = permissionMap.get(idAmbito);
			logger.debug("	flag write: " + ff.getWriteFlag());
			logger.debug("	flag advanced: " + ff.getAdvancedFlag());
		}
		return (permissionMap);
	}

	/**
	 * @param id_utente
	 * @param id_funzione
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public List<FunctionFlags> getFunctionFlagForFunction(Integer id_utente,
			Integer id_funzione) throws SQLException, DbAuthException {
		List<Integer> idGruppoList = readGroupFromFunction(id_funzione);
		List<Integer> userGroupList = readUserGroups(id_utente);
		List<FunctionFlags> ffList = new ArrayList<FunctionFlags>();
		for (int i = 0; i < userGroupList.size(); i++) {
			Integer idGruppo = userGroupList.get(i);
			if (idGruppoList.contains(idGruppo)) {
				ffList.add(readFunctionFlag(id_funzione, idGruppo));
			}
		}
		return ffList;
	}

	/**
	 * @param idFunzione
	 * @param idGruppo
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private FunctionFlags readFunctionFlag(Integer idFunzione, Integer idGruppo)
			throws SQLException, DbAuthException {
		Statement stmt = connection.createStatement();
		String query = "SELECT fn_scrittura, fn_avanzata FROM funzioni_gruppo"
				+ " WHERE id_funzione = " + qVal(idFunzione)
				+ " AND id_gruppo = " + qVal(idGruppo);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		FunctionFlags ff = null;
		while (rs.next()) {
			ff = new FunctionFlags();
			ff.setAdvancedFlag(rsGetBoolean(rs, rs.findColumn("fn_avanzata")));
			ff.setWriteFlag(rsGetBoolean(rs, rs.findColumn("fn_scrittura")));
		}
		rs.close();
		stmt.close();
		if (ff == null)
			throw new DbAuthException(
					"Non esiste nessuna entry nella tabella funzioni_gruppo "
							+ "con id_funzione = " + idFunzione
							+ " e id_gruppo = " + idGruppo);
		return ff;

	}

	/**
	 * @param id_funzione
	 * @return
	 * @throws SQLException
	 */
	private List<Integer> readGroupFromFunction(Integer id_funzione)
			throws SQLException {
		Statement stmt = connection.createStatement();
		String query = "SELECT id_gruppo FROM funzioni_gruppo WHERE id_funzione = "
				+ qVal(id_funzione);
		logger.debug(query);
		ResultSet rs = stmt.executeQuery(query);
		List<Integer> idGruppoList = new ArrayList<Integer>();
		while (rs.next()) {
			idGruppoList.add(rsGetInt(rs, rs.findColumn("id_gruppo")));
		}
		rs.close();
		stmt.close();
		return idGruppoList;
	}

	/*
	 * Ritorna, dati il nome di un utente e di una funzione, i flag associati
	 * alla funzione, ignorando gli ambiti a cui i flag sono associati.
	 */
	/*
	 * public FunctionFlags getFunctionFlags(String user, String function)
	 * throws SQLException, DbAuthException { Map<Integer, FunctionFlags>
	 * permissionMap = getPermissionMap(user, function); if
	 * (permissionMap.isEmpty()) return (null); FunctionFlags ff_result = new
	 * FunctionFlags(); Iterator<FunctionFlags> it_map =
	 * permissionMap.values().iterator(); while (it_map.hasNext()) {
	 * 
	 * FunctionFlags ff = it_map.next();
	 * ff_result.orWriteFlag(ff.getWriteFlag());
	 * ff_result.orAdvancedFlag(ff.getAdvancedFlag()); } return (ff_result); }
	 */

	/**
	 * @param ffList
	 * @return
	 */
	public FunctionFlags getFunctionFlag(List<FunctionFlags> ffList) {
		if (ffList == null || ffList.size() == 0)
			return null;
		else {
			// inizializzo a nessun privilegio
			FunctionFlags ffToReturn = new FunctionFlags();
			ffToReturn.setAdvancedFlag(false);
			ffToReturn.setWriteFlag(false);
			// per tutti gli elementi della lista se trovo un ff a true assumo
			// che per questa funzione abbia il privilegio
			for (int i = 0; i < ffList.size(); i++) {
				FunctionFlags ff = ffList.get(i);
				if (ff.getAdvancedFlag())
					ffToReturn.setAdvancedFlag(true);
				if (ff.getWriteFlag())
					ffToReturn.setWriteFlag(true);
			}
			logger.debug("	Advanced flag : " + ffToReturn.getAdvancedFlag());
			logger.debug("	Write    flag : " + ffToReturn.getWriteFlag());
			return ffToReturn;
		}

	}

	/*
	 * Ritorna la lista degli id_oggetto della tabella ambiti_acl relativi al
	 * tipo ogetto specificato per gli ambiti relativi all'utente specificato
	 * per la funzione specificata
	 */
	/**
	 * @param objType
	 * @param user
	 * @param function
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	public List<String> getObjIdListForUserFunc(Integer objType, String user,
			String function) throws SQLException, DbAuthException {
		Map<Integer, FunctionFlags> permissionMap = getPermissionMap(user,
				function);
		if (permissionMap.isEmpty())
			return (null);
		List<String> objIdList = new ArrayList<String>();
		Iterator<Integer> it_domain = permissionMap.keySet().iterator();
		while (it_domain.hasNext()) {
			Integer id_domain = it_domain.next();
			List<AmbitoAcl> ambitiAclList = readDomainAcl(id_domain, objType);
			if (ambitiAclList.size() > 0) {
				for (AmbitoAcl ambitoAcl : ambitiAclList)
					objIdList.add(ambitoAcl.getIdOggetto());
			}
		}
		logger.debug("Lista oggetti permessi:");
		for (String str : objIdList)
			logger.debug(str);

		return objIdList;
	}

	// TODO: usare un codificatore Base64 che non sia proprietario
	/**
	 * @param password
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String encryptPassword(String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA");
		md.update(password.getBytes("UTF-8"));
		return (new BASE64Encoder()).encode(md.digest());
	}

	/**
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static String encryptPasswordForApache(String password)
			throws Exception {
		String encryptedPwd = null;
		try {
			Process p = Runtime.getRuntime().exec(
					encryptCmdLine + " " + password);
			BufferedReader pis = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			encryptedPwd = pis.readLine();
			pis.close();
			if (encryptedPwd == null)
				throw new Exception("Errore: crittografia password "
						+ "fallita");
			return (encryptedPwd);
		} catch (java.io.IOException ex) {
			throw new Exception("Errore: crittografia password " + "fallita");

		}
	}

	/**
	 * @param salt
	 * @param password
	 * @param crypetdPassword
	 * @return
	 * @throws Exception
	 */
	public static Boolean isAuthorizedPasswordForApache(String salt,
			String password, String crypetdPassword) throws Exception {
		String encryptedPwd = null;
		try {
			Process p = Runtime.getRuntime().exec(
					verifyEncryptCmdLine + " " + salt + " " + password);
			BufferedReader pis = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			encryptedPwd = pis.readLine();
			pis.close();
			if (encryptedPwd == null)
				throw new Exception("Errore: crittografia password "
						+ "fallita");

			if (encryptedPwd.equals(crypetdPassword))
				return true;
			else
				return false;

		} catch (java.io.IOException ex) {
			throw new Exception("Errore: crittografia password " + "fallita");
		}
	}

	private Date rsGetDate(ResultSet rs, int column) throws SQLException {
		Date tmp;
		tmp = rs.getDate(column);
		if (rs.wasNull())
			return (null);
		return (tmp);
	}

	private Timestamp rsGetTimestamp(ResultSet rs, int column)
			throws SQLException {
		Timestamp tmp;
		tmp = rs.getTimestamp(column);
		if (rs.wasNull())
			return (null);
		return (tmp);
	}

	private Integer rsGetInt(ResultSet rs, int column) throws SQLException {
		Integer tmp = new Integer(rs.getInt(column));
		if (rs.wasNull())
			return (null);
		return (tmp);
	}

	private Boolean rsGetBoolean(ResultSet rs, int column) throws SQLException {
		Boolean tmp = new Boolean(rs.getBoolean(column));
		if (rs.wasNull())
			return (null);
		return (tmp);
	}

	private String rsGetString(ResultSet rs, int column) throws SQLException {
		String tmp = rs.getString(column);
		if (rs.wasNull())
			return (null);
		return (tmp);
	}

	private String qVal(String value) {
		if (value == null)
			return ("NULL");
		value = value.replaceAll("'", "\\\\'");
		return ("'" + value + "'");
	}

	private String qVal(Number value) {
		if (value == null)
			return ("NULL");
		return (value.toString());
	}

	private String qVal(Boolean value) {
		if (value == null)
			return ("NULL");
		return (value.toString());
	}

	private String qVal(java.sql.Date value) {
		if (value == null)
			return ("NULL");
		return ("'" + value + "'");
	}
}
