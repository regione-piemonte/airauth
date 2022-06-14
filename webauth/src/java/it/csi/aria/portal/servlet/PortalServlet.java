/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.portal.servlet;

import it.csi.aria.util.servlet.ApplicationException;
import it.csi.aria.util.servlet.HttpService;
import it.csi.aria.util.servlet.NotAuthorizedException;
import it.csi.aria.util.servlet.ServiceException;
import it.csi.aria.util.servlet.SessionExpiredException;
import it.csi.webauth.db.dao.DbAuth;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

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
 * 
 * Servlet for ariaweb portal
 * 
 * @author silvia.vergnano@consulenti.csi.it
 * 
 */

public class PortalServlet extends HttpService {

	private static final long serialVersionUID = -4394629677469803299L;

	private static final String APPNAME = "Ariaweb Portal";

	private static final String DS_NAME_AUTH = "dbAuth";

	private static final String PARTIAL_PORTAL_URL = "ariaweb_portal";

	private static Logger logger = Logger.getLogger("ariaweb.portal.servlet."
			+ PortalServlet.class.getSimpleName());

	private Template templateHeader;

	private Template templateFooter;

	private DataSource dataSourceAuth;

	public PortalServlet() throws Exception {
		super(logger);
		PropertyConfigurator.configure(getClass().getResource(
				"/log4j.properties"));
	}

	@Override
	public void init() throws ServletException {
		String classMethod = "[init] ";
		logger.info(classMethod + "Initializing " + APPNAME + " servlet");
		ServletContext sc = getServletContext();
		try {
			VelocityEngine ve = initVelocity();
			templateHeader = ve.getTemplate("header.vm");
			templateFooter = ve.getTemplate("footer.vm");
		} catch (Exception ex) {
			logger.error("Template engine initialization failure", ex);
			throw new ServletException(ex);
		}
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

		HttpSession session = request.getSession();
		Map reqMap = request.getParameterMap();
		String user = request.getRemoteUser();
		if (user == null)
			throw new NotAuthorizedException(
					"Impossibile ottenere il nome utente");
		session.setAttribute("user", user);
		logger.debug("Utente " + user + " autorizzato");
		UserPermissions perm = getUserPermissions(user);
		session.setAttribute("permissions", perm);
		session.setAttribute("portalPage", "manageUser");
		String portalPage = parseString(request, "portalPage", true);
		if (portalPage != null) {
			session.setAttribute("portalPage", portalPage);
		} else {
			portalPage = (String) session.getAttribute("portalPage");
		}
		logger.debug("Pagina corrente: " + portalPage);

		if ("manageUser".equals(portalPage)) {
			viewManageUser(request, response, session);
		} else {
			return false;
		}
		return true;
	}

	@Override
	protected boolean doPostImpl(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			ServiceException, ApplicationException, SessionExpiredException,
			NotAuthorizedException {
		HttpSession session = request.getSession();
		String portalPage = parseString(request, "portalPage", true);
		if (portalPage != null)
			session.setAttribute("portalPage", portalPage);
		else
			portalPage = (String) session.getAttribute("portalPage");

		String user = (String) session.getAttribute("user");

		if ("manageUser".equals(portalPage)) {
			logger.debug("3");
			viewManageUser(request, response, session);
		} else {
			logger.debug("5");
			return false;
		}

		return true;
	}

	@Override
	protected void printErrorHeader(PrintWriter pw) throws IOException {
		templateHeader.merge(null, pw);
	}

	@Override
	protected void printErrorFooter(PrintWriter pw) throws IOException {
		templateFooter.merge(null, pw);
	}

	private VelocityContext makeMenuContext(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException, ServiceException {
		UserPermissions perm = (UserPermissions) session
				.getAttribute("permissions");
		VelocityContext context = new VelocityContext();
		if (perm.ff_useradmin != null) {
			context.put(
					"manageUser",
					response.encodeURL(PARTIAL_PORTAL_URL
							+ "?portalPage=manageUser"));
		}
		context.put("home",
				response.encodeURL(PARTIAL_PORTAL_URL + "?portalPage=main"));
		return context;
	}

	private void viewManageUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException, ServiceException {
		logger.debug("Richiesta pagina gestione utente");
		VelocityContext ctxMenu = makeMenuContext(request, response, session);
		ctxMenu.put("manageUser", "");
		PrintWriter pw = getHtmlPrintWriter(response);
		templateHeader.merge(null, pw);

		// TODO: capire se bisogna usare encodeURL
		request.getRequestDispatcher("/webauth").include(request, response);
		templateFooter.merge(null, pw);
	}

	private UserPermissions getUserPermissions(String user)
			throws ApplicationException, IOException {
		DbAuth dbAuth = new DbAuth("dbauth", dataSourceAuth);
		try {
			dbAuth.connect();
			UserPermissions userP = new UserPermissions();
			userP.ff_useradmin = dbAuth.getFunctionFlag(dbAuth
					.getFunctionFlagForFunction(dbAuth.readUserId(user),
							dbAuth.readFunctionId("awpm")));
			return userP;
		} catch (Exception ex) {
			throw new ApplicationException(
					"[PortalServlet] Impossibile accedere alla banca"
							+ " dati dell'autenticazione", ex);
		} finally {
			try {
				dbAuth.disconnect();
			} catch (SQLException e) {
				logger.error("Error in disconnect", e);
			}
		}
	}
}
