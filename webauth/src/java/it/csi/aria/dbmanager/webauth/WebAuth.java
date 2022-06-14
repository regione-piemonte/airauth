/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.dbmanager.webauth;

import it.csi.aria.util.servlet.ApplicationException;
import it.csi.aria.util.servlet.HttpService;
import it.csi.aria.util.servlet.NotAuthorizedException;
import it.csi.aria.util.servlet.ServiceException;
import it.csi.aria.util.servlet.SessionExpiredException;
import it.csi.webauth.db.dao.DbAuth;
import it.csi.webauth.db.dao.DbAuthException;
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
import it.csi.webauth.db.model.WAUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public class WebAuth extends HttpService {

	private static final long serialVersionUID = -8285654722061124026L;

	private static final String VERSION = "1.0.0";

	private static final String APPNAME = "AriaWeb Webauth";

	private static final String DS_NAME_AUTH = "dbAuth";

	private static final String ADMIN_FN = "awpm";

	private static final String DAY_DATE_FMT = "dd/MM/yyyy";

	private static final String PARTIAL_PORTAL_URL = "ariaweb_portal";

	private static Logger logger = Logger.getLogger("ariaweb.webauth."
			+ WebAuth.class.getSimpleName());;

	private DataSource dataSourceAuth;

	private Template vmMain;

	private Template vmChangePwd;

	private Template vmManageUsers;

	private Template vmEditUser;

	private Template vmManageGroups;

	private Template vmManageDomains;

	private Template vmManageDomainAcl;

	private Template vmEditGroup;

	private Template vmEditDomain;

	private Template vmNewDomainAcl;

	private Template vmListGroupUsers;

	private Template vmOperationResult;

	public WebAuth() throws Exception {
		super(logger);
		PropertyConfigurator.configure(getClass().getResource(
				"/log4j.properties"));
		try {
			VelocityEngine ve = initVelocity();
			vmMain = ve.getTemplate("webauth/main.vm");
			vmChangePwd = ve.getTemplate("webauth/changePassword.vm");
			vmManageUsers = ve.getTemplate("webauth/manageUsers.vm");
			vmEditUser = ve.getTemplate("webauth/editUser.vm");
			vmManageGroups = ve.getTemplate("webauth/manageGroups.vm");
			vmManageDomains = ve.getTemplate("webauth/manageDomains.vm");
			vmManageDomainAcl = ve.getTemplate("webauth/manageDomainAcl.vm");
			vmEditGroup = ve.getTemplate("webauth/editGroup.vm");
			vmEditDomain = ve.getTemplate("webauth/editDomain.vm");
			vmNewDomainAcl = ve.getTemplate("webauth/newDomainAcl.vm");
			vmListGroupUsers = ve.getTemplate("webauth/listGroupUsers.vm");
			vmOperationResult = ve.getTemplate("webauth/operationResult.vm");
			// TODO: gestire i pulsanti di navigazione al fondo con una include
			// uguale per tutti
		} catch (Exception ex) {
			logger.error("Template engine initialization failure", ex);
		}
	}

	@Override
	public void init() throws ServletException {
		logger.info("Initializing " + APPNAME + " servlet, version " + VERSION);
		ServletContext sc = getServletContext();
		String dsNameAuth = getInitParam(sc, "dataSourceAuth", DS_NAME_AUTH);
		Context ctx;
		logger.info("Getting datasource " + dsNameAuth + "...");
		try {
			ctx = new InitialContext();
			dataSourceAuth = (DataSource) ctx.lookup("java:comp/env/"
					+ dsNameAuth);
		} catch (NamingException ex) {
			logger.error("Cannot get dataSource " + dsNameAuth + ": ", ex);
			throw new ServletException(ex);
		}
	}

	@Override
	protected boolean doGetImpl(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			ServiceException, ApplicationException, SessionExpiredException,
			NotAuthorizedException {
		logger.debug("GET called");
		HttpSession session = request.getSession();
		String user = (String) session.getAttribute("user");
		logger.debug("Utente: " + user);
		if (user == null)
			throw new NotAuthorizedException(
					"Impossibile ottenere il nome utente");
		DbAuth dbAuth = new DbAuth("dbauth", dataSourceAuth);
		try {
			dbAuth.connect();
			if (!dbAuth.checkUser(user))
				throw new NotAuthorizedException("L'utente " + user
						+ " non esiste");

			FunctionFlags ff_admin = getFfAdmin(dbAuth, user);

			Map<String, String[]> reqMap = 
					new HashMap<String, String[]>(request.getParameterMap());
			logRequestParams(reqMap);
			// Remove parameters belonging to the portal
			reqMap.remove("portalPage");
			if (reqMap.isEmpty()) // Default to 'main' in absence of parameters
				reqMap.put("main", new String[0]);

			if (reqMap.containsKey("changepwd")
					|| (ff_admin == null && reqMap.containsKey("main"))) {
				viewChangePwd(request, response, dbAuth, user);
			} else if (ff_admin == null)
				throw new NotAuthorizedException("L'utente " + user + " non è "
						+ "abilitato al cambio password");
			else if (reqMap.containsKey("changepwd")
					|| (!ff_admin.getAdvancedFlag() && reqMap
							.containsKey("main")))
				viewChangePwd(request, response, dbAuth, user);
			else if (!ff_admin.getAdvancedFlag())
				throw new NotAuthorizedException("L'utente " + user + " non è "
						+ "abilitato ad amministrare la banca dati degli "
						+ "utenti");
			else if (reqMap.containsKey("main")) {
				viewHome(request, response);
			} else if (reqMap.containsKey("manageusers"))
				viewManageUsers(request, response, dbAuth);
			else if (reqMap.containsKey("findusers"))
				viewFindUsers(request, response, dbAuth);
			else if (reqMap.containsKey("search_expired"))
				viewSearchExpired(request, response, dbAuth);
			else if (reqMap.containsKey("view_group"))
				viewGroups(request, response, dbAuth);
			else if (reqMap.containsKey("edituser"))
				viewEditUser(request, response, dbAuth);
			else if (reqMap.containsKey("managegroups"))
				viewManageGroups(request, response, dbAuth);
			else if (reqMap.containsKey("managedomains"))
				viewManageDomains(request, response, dbAuth);
			else if (reqMap.containsKey("managedomainacl"))
				viewManageDomainAcl(request, response, dbAuth);
			else if (reqMap.containsKey("editgroup"))
				viewEditGroup(request, response, dbAuth);
			else if (reqMap.containsKey("editdomain"))
				viewEditDomain(request, response, dbAuth);
			else if (reqMap.containsKey("editdomainacl"))
				viewNewDomainAcl(request, response, dbAuth);
			else if (reqMap.containsKey("listgroup"))
				viewListGroupUsers(request, response, dbAuth);
			else {
				return false;
			}
			return true;
		} catch (DbAuthException e) {
			logger.error(e);
			throw new ApplicationException("Errore durante l'accesso alla "
					+ "banca dati degli utenti", e);
		} catch (SQLException e) {
			logger.error(e);
			throw new ApplicationException("Errore durante l'accesso alla "
					+ "banca dati degli utenti", e);
		} finally {
			try {
				dbAuth.disconnect();
			} catch (SQLException e) {
				logger.error("Error closing DB connection", e);
			}
		}
	}

	/**
	 * @param dbAuth
	 * @param user
	 * @return
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private FunctionFlags getFfAdmin(DbAuth dbAuth, String user)
			throws SQLException, DbAuthException {
		List<FunctionFlags> ffList = dbAuth.getFunctionFlagForFunction(dbAuth
				.readUserId(user), dbAuth.readFunctionId(ADMIN_FN));
		FunctionFlags ff_admin = null;
		logger.debug("ffList.size " + ffList.size());
		if (ffList.size() != 0) {
			ff_admin = new FunctionFlags();
			ff_admin.setAdvancedFlag(false);
			ff_admin.setWriteFlag(false);

			Iterator<FunctionFlags> itFf = ffList.iterator();
			boolean found = false;
			while (itFf.hasNext() && !found) {
				FunctionFlags ff = itFf.next();
				if (ff.getAdvancedFlag()) {
					ff_admin.setAdvancedFlag(ff.getAdvancedFlag());
					found = true;
				}
			}// end while
			while (itFf.hasNext() && !found) {
				FunctionFlags ff = itFf.next();
				if (ff.getWriteFlag()) {
					ff_admin.setWriteFlag(ff.getWriteFlag());
					found = true;
				}
			}// end while
		}
		return ff_admin;
	}

	@Override
	protected boolean doPostImpl(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			ServiceException, ApplicationException, SessionExpiredException,
			NotAuthorizedException {
		logger.debug("POST called");
		HttpSession session = request.getSession();
		String user = (String) session.getAttribute("user");
		logger.debug("Utente: " + user);
		if (user == null)
			throw new NotAuthorizedException(
					"Impossibile ottenere il nome utente");
		DbAuth dbAuth = new DbAuth("dbauth", dataSourceAuth, true);
		try {
			dbAuth.connect();
			if (!dbAuth.checkUser(user))
				throw new NotAuthorizedException("L'utente " + user
						+ " non esiste");
			FunctionFlags ff_admin = getFfAdmin(dbAuth, user);
			Map<String, String[]> reqMap = request.getParameterMap();
			logRequestParams(reqMap);
			if (reqMap.containsKey("changepwd"))
				doChangePwd(request, response, dbAuth, user);
			else if (!ff_admin.getAdvancedFlag())
				throw new NotAuthorizedException("L'utente " + user + " non è "
						+ "abilitato ad amministrare la banca dati degli "
						+ "utenti");
			else if (reqMap.containsKey("writeuser"))
				doWriteUser(request, response, dbAuth, user);
			else if (reqMap.containsKey("deleteuser"))
				doDeleteUser(request, response, dbAuth);
			else if (reqMap.containsKey("newgroup"))
				doNewGroup(request, response, dbAuth, user);
			else if (reqMap.containsKey("updategroup"))
				doUpdateGroup(request, response, dbAuth, user);
			else if (reqMap.containsKey("deletegroup"))
				doDeleteGroup(request, response, dbAuth);
			else if (reqMap.containsKey("addfunction"))
				doAddFunction(request, response, dbAuth, user);
			else if (reqMap.containsKey("updatefunction"))
				doUpdateFunction(request, response, dbAuth, user);
			else if (reqMap.containsKey("deletefunction"))
				doDeleteFunction(request, response, dbAuth);
			else if (reqMap.containsKey("newdomain"))
				doNewDomain(request, response, dbAuth, user);
			else if (reqMap.containsKey("newdomainacl"))
				doNewDomainAcl(request, response, dbAuth, user);
			else if (reqMap.containsKey("updatedomain"))
				doUpdateDomain(request, response, dbAuth, user);
			else if (reqMap.containsKey("deletedomain"))
				doDeleteDomain(request, response, dbAuth);
			else if (reqMap.containsKey("deletedomainacl"))
				doDeleteDomainAcl(request, response, dbAuth);
			else
				return false;
			return true;
		} catch (DbAuthException e) {
			logger.error(e);
			throw new ApplicationException("Errore durante l'accesso alla "
					+ "banca dati degli utenti", e);
		} catch (SQLException e) {
			logger.error(e);
			throw new ApplicationException("Errore durante l'accesso alla "
					+ "banca dati degli utenti", e);
		} finally {
			try {
				dbAuth.disconnect();
			} catch (SQLException e) {
				logger.error("Error closing DB connection", e);
			}
		}
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void viewHome(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		VelocityContext ctx = new VelocityContext();
		ctx.put("users", response
				.encodeURL(PARTIAL_PORTAL_URL + "?manageusers"));
		ctx.put("groups", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managegroups"));
		ctx.put("domains", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomains"));
		ctx.put("domainacl", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomainacl"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmMain.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws SQLException
	 * @throws IOException
	 * @throws DbAuthException
	 */
	private void viewChangePwd(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws SQLException, IOException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer userId = dbAuth.readUserId(user);
		Utente utente = dbAuth.readUser(userId);
		List<Ambito> list_domains = dbAuth.readDomainList();
		List<Gruppo> list_groups = dbAuth.readGroupList();
		List<Integer> list_user_groups = dbAuth.readUserGroups(utente
				.getIdUtente());
		selectGroupsForUser(list_groups, list_user_groups);
		ctx.put("utente", utente);
		ctx.put("list_domains", list_domains);
		ctx.put("list_groups", list_groups);
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmChangePwd.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws SQLException
	 * @throws IOException
	 */
	private void viewManageUsers(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth) throws SQLException,
			IOException {
		VelocityContext ctx = new VelocityContext();
		List<Gruppo> list_groups = dbAuth.readGroupList();
		ctx.put("list_groups", list_groups);
		ctx.put("users", new ArrayList<Utente>());
		ctx.put("message", "");
		ctx.put("expiryDate", new SimpleDateFormat(DAY_DATE_FMT)
				.format(new Date()));
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmManageUsers.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 */
	private void viewFindUsers(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException {
		VelocityContext ctx = new VelocityContext();
		String userKey = parseString(request, "findusers", true);
		List<Gruppo> list_groups = dbAuth.readGroupList();
		List<Utente> users = dbAuth.readUserList(userKey);
		ctx.put("users", users);
		ctx.put("list_groups", list_groups);
		ctx.put("message", "");
		ctx.put("expiryDate", new SimpleDateFormat(DAY_DATE_FMT)
				.format(new Date()));
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmManageUsers.merge(ctx, pw);
	}

	// TODO: gestire in modo comprensibile per l'utente il caso in cui la data
	// viene scritta in modo sbagliato
	// TODO: verificare il funzionamento generale della funzione
	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 */
	private void viewSearchExpired(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException {
		VelocityContext ctx = new VelocityContext();
		DateFormat df = new SimpleDateFormat(DAY_DATE_FMT);
		Date dateExpired = parseDate(request, "date_expired", true, df);
		if (dateExpired == null)
			dateExpired = new Date();
		List<Gruppo> list_groups = dbAuth.readGroupList();
		List<Utente> listExpiredUsers = dbAuth.readExpiredUserList(dateExpired);
		ctx.put("message", "");
		ctx.put("users", listExpiredUsers);
		ctx.put("list_groups", list_groups);
		ctx.put("expiryDate", parseString(request, "date_expired", true));
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmManageUsers.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 */
	private void viewGroups(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException {
		VelocityContext ctx = new VelocityContext();
		Integer groupId = parseInt(request, "select_group", false);
		List<Gruppo> list_groups = dbAuth.readGroupList();
		List<Utente> list_group = dbAuth.readUserListForGroup(groupId);
		ctx.put("message", "");
		ctx.put("users", list_group);
		ctx.put("list_groups", list_groups);
		ctx.put("expiryDate", new SimpleDateFormat(DAY_DATE_FMT)
				.format(new Date()));
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmManageUsers.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void viewEditUser(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer userId = parseInt(request, "edituser", true);
		Utente utente = null;
		if (userId == null) {
			utente = new Utente();
			utente.setAbilitazione(new Boolean(true));
		} else
			utente = dbAuth.readUser(userId);
		List<Ambito> list_domains = dbAuth.readDomainList();
		List<Integer> listIdAmbitiUtente = dbAuth.readUserDomain(userId);
		List<String> listAmbitiUtente = new ArrayList<String>();
		for (int i = 0; i < listIdAmbitiUtente.size(); i++) {
			Integer idAmbito = listIdAmbitiUtente.get(i);
			listAmbitiUtente.add(dbAuth.readDomain(idAmbito).getAmbito());
		}
		List<Gruppo> list_groups = dbAuth.readGroupList();
		List<Integer> list_user_groups = utente.getIdUtente() == null ? new ArrayList<Integer>()
				: dbAuth.readUserGroups(utente.getIdUtente());
		selectGroupsForUser(list_groups, list_user_groups);
		ctx.put("utente", utente);
		ctx.put("list_domains", list_domains);
		ctx.put("list_groups", list_groups);
		ctx.put("list_user_domains", listAmbitiUtente);
		ctx.put("dateFmt", new SimpleDateFormat(DAY_DATE_FMT));
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?manageusers"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmEditUser.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws SQLException
	 * @throws IOException
	 */
	private void viewManageGroups(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth) throws SQLException,
			IOException {
		VelocityContext ctx = new VelocityContext();
		List<Gruppo> list_groups = dbAuth.readGroupList();
		ctx.put("message", "");
		ctx.put("users", new ArrayList<Utente>());
		ctx.put("list_groups", list_groups);
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmManageGroups.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws SQLException
	 * @throws IOException
	 */
	private void viewManageDomains(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth) throws SQLException,
			IOException {
		VelocityContext ctx = new VelocityContext();
		List<Ambito> list_domains = dbAuth.readDomainList();
		ctx.put("list_domains", list_domains);
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmManageDomains.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws SQLException
	 * @throws IOException
	 * @throws DbAuthException
	 */
	private void viewManageDomainAcl(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth) throws SQLException,
			IOException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		List<AmbitoAcl> listDomainAcl = dbAuth.readDomainAclList();
		List<HashMap<String, String>> list_domain_acl = new ArrayList<HashMap<String, String>>();
		ListIterator<AmbitoAcl> li_acl = listDomainAcl.listIterator();
		while (li_acl.hasNext()) {
			AmbitoAcl ambitoAcl = li_acl.next();
			HashMap<String, String> hm_ambitoAcl = new HashMap<String, String>();
			Ambito ambito = dbAuth.readDomain(ambitoAcl.getIdAmbito());
			hm_ambitoAcl.put("domain", ambito.getDescrizione());
			boolean found = false;
			List<TipoOggetto> objTypeList = dbAuth.readTypeObjectList();
			logger.debug("objTypeList.size:" + objTypeList.size());
			for (int i = 0; i < objTypeList.size() && !found; i++) {
				logger.debug("ambitoAcl.getIdTipoOggetto():"
						+ ambitoAcl.getIdTipoOggetto());
				if (objTypeList.get(i) != null
						&& objTypeList.get(i).getIdTipoOggetto().equals(
								ambitoAcl.getIdTipoOggetto())) {
					found = true;
					if (objTypeList.get(i) != null)
						hm_ambitoAcl.put("objType", objTypeList.get(i)
								.getDescrizione());
					else
						hm_ambitoAcl.put("objType", objTypeList.get(i)
								.getIdTipoOggetto().toString());
				}
			}
			if (!found)
				hm_ambitoAcl.put("objType", dbAuth.readTypeObjectDesc(
						ambitoAcl.getIdTipoOggetto()).toString());
			hm_ambitoAcl.put("objName", ambitoAcl.getIdOggetto());
			hm_ambitoAcl.put("ambitoAclKey", ambitoAcl.getIdAmbito() + "|"
					+ ambitoAcl.getIdTipoOggetto() + "|"
					+ ambitoAcl.getIdOggetto());
			list_domain_acl.add(hm_ambitoAcl);
		}
		ctx.put("list_domain_acl", list_domain_acl);
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmManageDomainAcl.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void viewEditGroup(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer groupId = parseInt(request, "editgroup", true);
		Gruppo gruppo = null;
		List<Ambito> list_domains = dbAuth.readDomainList();
		List<Funzione> list_functions = dbAuth.readFunctionList();
		List<FunzioneGruppo> list_grfuns = null;
		if (groupId == null) {
			gruppo = new Gruppo();
			list_grfuns = new ArrayList<FunzioneGruppo>();
		} else {
			gruppo = dbAuth.readGroup(groupId);
			List<FunzioneGruppo> tmp_list_grfuns = dbAuth
					.readGroupFunctions(groupId);
			list_grfuns = WAUtil.orderGroupFunctions(list_functions,
					tmp_list_grfuns);
		}
		ctx.put("WAUtil", new WAUtil());
		ctx.put("gruppo", gruppo);
		ctx.put("list_grfuns", list_grfuns);
		Map<Integer, List<FunzioniGruppoAmbito>> mapFunzGrupAmbiti = new HashMap<Integer, List<FunzioniGruppoAmbito>>();
		Iterator<FunzioneGruppo> itListGRuFuns = list_grfuns.iterator();
		while (itListGRuFuns.hasNext()) {
			FunzioneGruppo funzGruppo = itListGRuFuns.next();
			mapFunzGrupAmbiti.put(funzGruppo.getIdFunzione(), dbAuth
					.readGroupFunctionsDomains(funzGruppo));
		}
		ctx.put("mapFunzGrupAmbiti", mapFunzGrupAmbiti);
		ctx.put("list_domains", list_domains);
		ctx.put("list_functions", list_functions);
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managegroups"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmEditGroup.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void viewEditDomain(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer domainId = parseInt(request, "editdomain", true);
		Ambito domain = null;
		if (domainId == null)
			domain = new Ambito();
		else
			domain = dbAuth.readDomain(domainId);
		ctx.put("ambito", domain);
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomains"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmEditDomain.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void viewNewDomainAcl(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		List<TipoOggetto> objTypeList = dbAuth.readTypeObjectList();
		ctx.put("objTypeList", objTypeList);
		ctx.put("domainList", dbAuth.readDomainList());
		ctx.put("action", response.encodeURL(PARTIAL_PORTAL_URL));
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomainacl"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmNewDomainAcl.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void viewListGroupUsers(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer id_gruppo = parseInt(request, "listgroup", false);
		Gruppo gruppo = dbAuth.readGroup(id_gruppo);
		List<Utente> list_user_groups = dbAuth.readUserListForGroup(id_gruppo);
		ctx.put("result", new Boolean(true));
		ctx.put("message", "Questi utenti appartengono al gruppo: "
				+ gruppo.getGruppo());
		ctx.put("lista_utenti", list_user_groups);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managegroups"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmListGroupUsers.merge(ctx, pw);
	}

	// TODO: verificare come vengono gestiti gli errori di crittografia password
	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws NotAuthorizedException
	 * @throws ApplicationException
	 * @throws DbAuthException
	 */
	private void doChangePwd(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, NotAuthorizedException, ApplicationException,
			DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer userId = user == null ? null : dbAuth.readUserId(user);
		Integer chPwdUserId = parseInt(request, "changepwd", true);
		if (userId == null || chPwdUserId == null
				|| !userId.equals(chPwdUserId)) {
			throw new NotAuthorizedException("L'utente " + user + " non è "
					+ "abilitato a cambiare la password dell'utente "
					+ chPwdUserId);
		}
		Integer autore_agg = dbAuth.readUserId(user);
		Integer user_id = parseInt(request, "changepwd", false);
		Utente utente = dbAuth.readUser(user_id);
		utente.setPassword(makePassword(
				parseString(request, "password", false), parseString(request,
						"conferma_password", false)));
		utente.setAutoreAgg(autore_agg);
		dbAuth.updateUser(utente);
		String message = "La password per l'utente " + utente.getUtente()
				+ " e' stata aggiornata correttamente";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("home", response.encodeURL(PARTIAL_PORTAL_URL + "?main"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws ApplicationException
	 * @throws DbAuthException
	 */
	private void doWriteUser(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, ApplicationException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer autore_agg = dbAuth.readUserId(user);
		Integer writeuser = parseInt(request, "writeuser", true);
		boolean new_user = (writeuser == null);
		Utente utente = new Utente();
		utente.setIdUtente(new_user ? null : writeuser);
		utente.setUtente(new_user ? parseString(request, "utente", false)
				: null);
		utente.setPassword(makePassword(
				parseString(request, "password", false), parseString(request,
						"conferma_password", false)));
		utente.setAbilitazione(parseBoolean(request, "abilitazione", false));
		String strDs = parseString(request, "data_scadenza", true);
		Date ds = null;
		if (!"nessuna".equalsIgnoreCase(strDs))
			ds = parseDate(request, "data_scadenza", true,
					new SimpleDateFormat(DAY_DATE_FMT));
		utente.setDataScadenza(ds == null ? null : new java.sql.Date(ds
				.getTime()));
		utente.setNome(parseString(request, "nome", true));
		utente.setCognome(parseString(request, "cognome", true));
		utente.setAzienda(parseString(request, "azienda", true));
		utente.setMail(parseString(request, "mail", true));
		utente.setTelefono(parseString(request, "telefono", true));
		utente.setIndirizzo(parseString(request, "indirizzo", true));
		utente.setAutoreAgg(autore_agg);
		if (new_user) {
			if (dbAuth.checkUser(utente.getUtente())) {
				String message = "Il nome utente " + utente.getUtente()
						+ " e' gia' in uso, specificarne uno differente";
				ctx.put("result", new Boolean(false));
				ctx.put("message", message);
				ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
						+ "?edituser"));
				PrintWriter pw = getHtmlPrintWriter(response);
				vmOperationResult.merge(ctx, pw);
				return;
			}
			dbAuth.insertUser(utente);
		} else
			dbAuth.updateUser(utente);
		String str_utente = new_user ? utente.getUtente() : dbAuth
				.readUsername(utente.getIdUtente());
		Integer id_utente = new_user ? dbAuth.readUserId(utente.getUtente())
				: utente.getIdUtente();
		List<Gruppo> list_groups = dbAuth.readGroupList();
		List<Integer> list_user_groups = dbAuth.readUserGroups(id_utente);
		selectGroupsForUser(list_groups, list_user_groups);
		Iterator<Gruppo> it_gr = list_groups.iterator();
		while (it_gr.hasNext()) {
			Gruppo gr = (Gruppo) it_gr.next();
			if (gr.getSelected()) {
				if ("on".equals(parseString(request, gr.getGruppo(), true)))
					continue;
				dbAuth.deleteUserGroup(id_utente, gr.getIdGruppo());
			} else {
				if (!"on".equals(parseString(request, gr.getGruppo(), true)))
					continue;
				GruppoUtente gu = new GruppoUtente();
				gu.setIdUtente(id_utente);
				gu.setIdGruppo(gr.getIdGruppo());
				gu.setAutoreAgg(autore_agg);
				dbAuth.insertUserGroup(gu);
			}
		}

		// salvataggio info su ambito
		if (!new_user) {
			// delete
			dbAuth.deleteUserFromDomain(utente.getIdUtente());
		} else {
			// read new id_utente
			utente.setIdUtente(dbAuth.readUserId(utente.getUtente()));
		}
		// insert
		String[] selectedAmbiti = request.getParameterValues("ambito");
		if (selectedAmbiti != null) {
			for (int i = 0; i < selectedAmbiti.length; i++) {
				AmbitoUtente ambitoUtente = new AmbitoUtente();
				Integer idAmbito = dbAuth.readDomainId(selectedAmbiti[i]);
				ambitoUtente.setIdAmbito(idAmbito);
				ambitoUtente.setIdUtente(utente.getIdUtente());
				ambitoUtente.setAutoreAgg(dbAuth.readUserId(user));
				// insert
				dbAuth.insertUserDomain(ambitoUtente);
			}
		}
		String message = "Le informazioni per l'utente " + str_utente
				+ " sono state inserite nella banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?manageusers"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doDeleteUser(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer deleteuser = parseInt(request, "deleteuser", false);
		dbAuth.deleteUserFromDomain(deleteuser);
		dbAuth.deleteUser(deleteuser);
		String message = "L'utente selezionato e' stato cancellato "
				+ "dalla banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?manageusers"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doNewGroup(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Gruppo gruppo = new Gruppo();
		gruppo.setGruppo(parseString(request, "newgroup", false));
		gruppo.setDescrizione(parseString(request, "descrizione", true));
		gruppo.setAutoreAgg(dbAuth.readUserId(user));
		if (dbAuth.checkGroup(gruppo.getGruppo())) {
			String message = "Il gruppo " + gruppo.getGruppo()
					+ " e' gia' in uso, specificarne uno differente";
			ctx.put("result", new Boolean(false));
			ctx.put("message", message);
			ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
					+ "?editgroup"));
			PrintWriter pw = getHtmlPrintWriter(response);
			vmOperationResult.merge(ctx, pw);
			return;
		}
		dbAuth.insertGroup(gruppo);
		String message = "Il gruppo " + gruppo.getGruppo()
				+ " e' stato inserito nella banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managegroups"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doUpdateGroup(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Gruppo gruppo = new Gruppo();
		gruppo.setIdGruppo(parseInt(request, "updategroup", false));
		gruppo.setDescrizione(parseString(request, "descrizione", true));
		gruppo.setAutoreAgg(dbAuth.readUserId(user));
		dbAuth.updateGroup(gruppo);
		String message = "Le informazioni del gruppo selezionato sono state "
				+ "aggiornate nella banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managegroups"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doDeleteGroup(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer id_gruppo = parseInt(request, "deletegroup", false);
		dbAuth.deleteGroup(id_gruppo);
		String message = "Il gruppo selezionato e' stato cancellato "
				+ "dalla banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managegroups"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doAddFunction(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		FunzioneGruppo fg = new FunzioneGruppo();
		fg.setIdGruppo(parseInt(request, "group_id", false));
		fg.setIdFunzione(parseInt(request, "funzione", false));
		// fg.setIdAmbito(parseInt(request, "ambito", false));
		fg.setFnScrittura(parseBoolean(request, "scrittura", false));
		fg.setFnAvanzata(parseBoolean(request, "avanzata", false));
		fg.setAutoreAgg(dbAuth.readUserId(user));
		dbAuth.insertGroupFunction(fg);
		//aggiungo gli ambiti che dovranno essere associati alla funzione e al gruppo
		String[] selectedAmbiti = request.getParameterValues("ambito");
		if (selectedAmbiti != null) {
			for (int i = 0; i < selectedAmbiti.length; i++) {
				FunzioniGruppoAmbito fga = null;
				int ambitoSel = new Integer(selectedAmbiti[i]).intValue();
				if (ambitoSel != -1) {
					fga = new FunzioniGruppoAmbito();
					// ho degli ambiti selezionati da inserire
					fga.setIdAmbito(new Integer(selectedAmbiti[i]));
					fga.setIdFunzione(fg.getIdFunzione());
					fga.setIdGruppo(fg.getIdGruppo());
					dbAuth.insertGroupFunctionDomain(fga);
				}
			}// end for
		}// end if
		
		
		String message = "Funzione ed eventuali ambiti associati aggiunti correttamente";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?editgroup=" + fg.getIdGruppo()));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doUpdateFunction(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		FunzioneGruppo fg = new FunzioneGruppo();
		fg.setIdGruppo(parseInt(request, "group_id", false));
		fg.setIdFunzione(parseInt(request, "funzione", false));
		fg.setFnScrittura(parseBoolean(request, "scrittura", false));
		fg.setFnAvanzata(parseBoolean(request, "avanzata", false));
		fg.setAutoreAgg(dbAuth.readUserId(user));
		dbAuth.updateGroupFunction(fg);
		// delete e insert nella tabella funzioni_gruppi_ambiti
		dbAuth.deleteDomainFromGroupFunction(fg.getIdFunzione(), fg
				.getIdGruppo());
		String[] selectedAmbiti = request.getParameterValues("ambito_selected");
		if (selectedAmbiti != null) {
			for (int i = 0; i < selectedAmbiti.length; i++) {
				FunzioniGruppoAmbito fga = null;
				int ambitoSel = new Integer(selectedAmbiti[i]).intValue();
				if (ambitoSel != -1) {
					fga = new FunzioniGruppoAmbito();
					// ho degli ambiti selezionati da inserire
					fga.setIdAmbito(new Integer(selectedAmbiti[i]));
					fga.setIdFunzione(fg.getIdFunzione());
					fga.setIdGruppo(fg.getIdGruppo());
					dbAuth.insertGroupFunctionDomain(fga);
				}
			}// end for
		}// end if

		String message = "Funzione e ambiti associati aggiornati correttamente";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?editgroup=" + fg.getIdGruppo()));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doDeleteFunction(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer group_id = parseInt(request, "group_id", false);
		Integer function_id = parseInt(request, "funzione",
				false);
		dbAuth.deleteDomainFromGroupFunction(function_id,group_id);
		dbAuth.deleteGroupFunction(group_id, function_id);

		String message = "Funzione cancellata correttamente";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?editgroup=" + group_id));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doNewDomain(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Ambito ambito = new Ambito();
		ambito.setAmbito(parseString(request, "newdomain", false));
		ambito.setDescrizione(parseString(request, "descrizione", true));
		ambito.setAutoreAgg(dbAuth.readUserId(user));
		if (dbAuth.checkDomain(ambito.getAmbito())) {
			String message = "L'ambito " + ambito.getAmbito()
					+ " e' gia' in uso, specificarne uno differente";
			ctx.put("result", new Boolean(false));
			ctx.put("message", message);
			ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
					+ "?editdomain"));
			PrintWriter pw = getHtmlPrintWriter(response);
			vmOperationResult.merge(ctx, pw);
			return;
		}
		dbAuth.insertDomain(ambito);
		String message = "L'ambito " + ambito.getAmbito()
				+ " e' stato inserito nella banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomains"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doUpdateDomain(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Ambito ambito = new Ambito();
		ambito.setIdAmbito(parseInt(request, "updatedomain", false));
		ambito.setDescrizione(parseString(request, "descrizione", true));
		ambito.setAutoreAgg(dbAuth.readUserId(user));
		dbAuth.updateDomain(ambito);
		String message = "Le informazioni dell'ambito selezionato sono state "
				+ "aggiornate nella banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomains"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doDeleteDomain(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		Integer id_ambito = parseInt(request, "deletedomain", false);
		dbAuth.deleteDomainFromUserDomain(id_ambito);
		dbAuth.deleteDomain(id_ambito);
		String message = "L'ambito selezionato e' stato cancellato "
				+ "dalla banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomains"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @param user
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doNewDomainAcl(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth, String user)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		AmbitoAcl ambitoAcl = new AmbitoAcl();
		ambitoAcl.setIdAmbito(parseInt(request, "id_ambito", false));
		ambitoAcl.setIdTipoOggetto(parseInt(request, "tipo_oggetto", false));
		ambitoAcl.setIdOggetto(parseString(request, "id_oggetto", false));
		ambitoAcl.setAutoreAgg(dbAuth.readUserId(user));

		if (dbAuth.checkDomainAcl(ambitoAcl)) {
			String message = "L'ambito acl e' gia' in uso, specificarne uno differente";
			ctx.put("result", new Boolean(false));
			ctx.put("message", message);
			ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
					+ "?newdomainacl"));
			PrintWriter pw = getHtmlPrintWriter(response);
			vmOperationResult.merge(ctx, pw);
			return;
		}
		dbAuth.insertDomainAcl(ambitoAcl);
		String message = "L'ambito acl e' stato inserito nella banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomainacl"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param request
	 * @param response
	 * @param dbAuth
	 * @throws ServletException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws SQLException
	 * @throws DbAuthException
	 */
	private void doDeleteDomainAcl(HttpServletRequest request,
			HttpServletResponse response, DbAuth dbAuth)
			throws ServletException, IOException, ServiceException,
			SQLException, DbAuthException {
		VelocityContext ctx = new VelocityContext();
		String domainAclKey = parseString(request, "deletedomainacl", false);
		AmbitoAcl domainAcl = new AmbitoAcl();
		String[] domainAclKeyStrArray = domainAclKey.split("\\|");
		domainAcl.setIdAmbito(new Integer(domainAclKeyStrArray[0]));
		domainAcl.setIdTipoOggetto(new Integer(domainAclKeyStrArray[1]));
		domainAcl.setIdOggetto(domainAclKeyStrArray[2]);
		dbAuth.deleteDomainAcl(domainAcl);
		String message = "L'ambito acl selezionato e' stato cancellato "
				+ "dalla banca dati";
		ctx.put("result", new Boolean(true));
		ctx.put("message", message);
		ctx.put("upTarget", response.encodeURL(PARTIAL_PORTAL_URL
				+ "?managedomainacl"));
		PrintWriter pw = getHtmlPrintWriter(response);
		vmOperationResult.merge(ctx, pw);
	}

	/**
	 * @param list_groups
	 * @param list_user_groups
	 */
	private void selectGroupsForUser(List<Gruppo> list_groups,
			List<Integer> list_user_groups) {
		Set<Integer> set_user_groups = new HashSet<Integer>(list_user_groups);
		Iterator<Gruppo> it_gr = list_groups.iterator();
		while (it_gr.hasNext()) {
			Gruppo gr = it_gr.next();
			gr.setSelected(set_user_groups.contains(gr.getIdGruppo()));
		}
	}

	/**
	 * @param pwd
	 * @param pwd2
	 * @return
	 * @throws ApplicationException
	 */
	private String makePassword(String pwd, String pwd2)
			throws ApplicationException {
		if ((pwd == null || pwd.isEmpty()) && (pwd2 == null || pwd2.isEmpty()))
			return null;
		if (pwd == null || !pwd.equals(pwd2))
			throw new ApplicationException("Impossibile impostare la password:"
					+ " la password e la sua conferma sono diverse");
		if (pwd.length() < 6)
			throw new ApplicationException("Impossibile impostare la password:"
					+ " la lunghezza minima per la password è 6 caratteri");

		try {
			String enc_pwd = DbAuth.encryptPasswordForApache(pwd);
			logger.debug("Encrypted password: " + enc_pwd);
			return enc_pwd;
		} catch (Exception e) {
			throw new ApplicationException("Impossibile impostare la password:"
					+ " errore nell'algoritmo di crittografia", e);
		}
	}

}
