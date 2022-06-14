/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.util.servlet;

import it.csi.webauth.db.dao.DbAuth;
import it.csi.webauth.db.model.FunctionFlags;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public abstract class HttpService extends HttpServlet {

	private static final long serialVersionUID = -3819878434644375797L;

	private static final String PARTIAL_PORTAL_URL = "ariaweb_portal";

	// ATTENTION: SimpleDateFormat parse and format functions are not reentrant
	private final DateFormat requestDateFmt = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private final DateFormat requestTzDateFmt = new SimpleDateFormat(
			"yyyyMMddHHmmssZ");

	private static final String MSG_INV_PARAM_FORMAT = "Formato non valido per il parametro ";

	private static final String MSG_PARAM_MISSING = "Parametro della richiesta mancante ";

	private Logger logger;

	private Set<String> setAllowedIP;

	private Template templateError;

	public HttpService(Logger logger) throws Exception {
		this();
		this.logger = logger;
		this.setAllowedIP = null;
	}

	public HttpService(Logger logger, List<String> listAllowedHosts)
			throws Exception {
		this();
		this.logger = logger;
		this.setAllowedIP = new HashSet<String>();
		for (String host : listAllowedHosts) {
			try {
				InetAddress iAddr = InetAddress.getByName(host);
				String strAddr = iAddr.getHostAddress();
				if (strAddr != null) {
					setAllowedIP.add(strAddr);
					logger.info("Host added to allowed hosts list: " + strAddr);
				}
			} catch (Exception e) {
				logger.error("Invalid host in allowed hosts list: " + host);
			}
		}
	}

	private HttpService() throws Exception {
		VelocityEngine ve = initVelocity();
		templateError = ve.getTemplate("error.vm");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			checkRemotePermissions(request);
			if (!doGetImpl(request, response))
				throw new ServiceException("Richiesta pagina non esistente");
		} catch (SessionExpiredException e) {
			String msg = "La sessione corrente è scaduta";
			logger.warn(msg, e);
			sendError(response, "Avviso", msg, "Torna alla pagina di Login",
					PARTIAL_PORTAL_URL + "?portalPage=login");
		} catch (NotAuthorizedException e) {
			String msg = "Operazione non autorizzata";
			logger.warn(msg, e);
			sendError(response, "Avviso", msg + ":" + e.getMessage(),
					"Torna alla pagina di Login", PARTIAL_PORTAL_URL
							+ "?portalPage=login");
		} catch (ServiceException e) {
			String msg = "Si è verificato un errore nell'interpretazione "
					+ "della richiesta";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		} catch (IllegalArgumentException e) {
			String msg = "Parametri della richiesta errati";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		} catch (ApplicationException e) {
			String msg = "Si è verificato un errore nel servizio web";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		} catch (Exception e) {
			String msg = "Si è verificato un errore non classificato";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		}
	}

	protected abstract boolean doGetImpl(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			ServiceException, SessionExpiredException, NotAuthorizedException,
			ApplicationException;

	// NOTE: doPost may be tested using curl
	// curl --noproxy host --data-binary "@filename" http://host:port/webui
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			checkRemotePermissions(request);
			boolean dopost = doPostImpl(request, response);
			if (!dopost)
				throw new ServiceException("Richiesta pagina non esistente");
		} catch (SessionExpiredException e) {
			String msg = "La sessione corrente è scaduta";
			logger.warn(msg, e);
			sendError(response, "Avviso", msg, "Torna alla pagina di Login",
					PARTIAL_PORTAL_URL + "?portalPage=login");
		} catch (NotAuthorizedException e) {
			String msg = "Operazione non autorizzata";
			logger.warn(msg, e);
			sendError(response, "Avviso", msg + ":" + e.getMessage(),
					"Torna alla pagina di Login", PARTIAL_PORTAL_URL
							+ "?portalPage=login");
		} catch (ServiceException e) {
			String msg = "Si è verificato un errore nell'interpretazione "
					+ "della richiesta";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		} catch (IllegalArgumentException e) {
			String msg = "Parametri della richiesta errati";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		} catch (ApplicationException e) {
			String msg = "Si è verificato un errore nel servizio web";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		} catch (Exception e) {
			String msg = "Si è verificato un errore non classificato";
			logger.error(msg, e);
			sendError(response, "Errore", msg + ":" + e.getMessage(),
					"Torna alla pagina iniziale", PARTIAL_PORTAL_URL
							+ "?portalPage=home");
		}
	}

	protected abstract boolean doPostImpl(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			ServiceException, SessionExpiredException, NotAuthorizedException,
			ApplicationException;

	private void sendError(HttpServletResponse response, String title,
			String message, String redirectMessage, String redirectTarget) {
		try {
			VelocityContext context = new VelocityContext();
			context.put("title", title);
			context.put("message", message);
			context.put("redirectMessage", redirectMessage);
			context.put("redirectTarget", response.encodeURL(redirectTarget));
			PrintWriter pw = getHtmlPrintWriter(response);
			printErrorHeader(pw);
			templateError.merge(context, pw);
			printErrorFooter(pw);
		} catch (IOException e) {
			logger.error("Cannot send error message '" + message
					+ "' to the client", e);
		}
	}

	protected void printErrorHeader(PrintWriter pw) throws IOException {
	}

	protected void printErrorFooter(PrintWriter pw) throws IOException {
	}

	protected String getInitParam(ServletContext context, String name,
			String defaultValue) {
		String value = context.getInitParameter(name);
		if (value == null)
			value = getInitParameter(name);
		if (value == null)
			value = defaultValue;
		return value;
	}

	protected String parseString(HttpServletRequest request, String paramName,
			boolean optional) throws ServletException, IOException,
			ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		return paramValue;
	}

	protected Integer parseInt(HttpServletRequest request, String paramName,
			boolean optional) throws ServletException, IOException,
			ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null || paramValue.trim().isEmpty()) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			return Integer.parseInt(paramValue.trim());
		} catch (NumberFormatException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	protected Double parseDouble(HttpServletRequest request, String paramName,
			boolean optional) throws ServletException, IOException,
			ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null || paramValue.trim().isEmpty()) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			return Double.parseDouble(paramValue.trim());
		} catch (NumberFormatException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	protected Boolean parseBoolean(HttpServletRequest request,
			String paramName, boolean optional) throws ServletException,
			IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null || paramValue.trim().isEmpty()) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		return Boolean.parseBoolean(paramValue.trim());
	}

	protected Long parseLong(HttpServletRequest request, String paramName,
			boolean optional) throws ServletException, IOException,
			ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null || paramValue.trim().isEmpty()) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			return Long.parseLong(paramValue.trim());
		} catch (NumberFormatException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	protected Date parseDate(HttpServletRequest request, String paramName,
			boolean optional) throws ServletException, IOException,
			ServiceException {
		return parseDate(request, paramName, optional, requestDateFmt);
	}

	protected Date parseDateWithTimeZone(HttpServletRequest request,
			String paramName, boolean optional) throws ServletException,
			IOException, ServiceException {
		return parseDate(request, paramName, optional, requestTzDateFmt);
	}

	protected Date parseDate(HttpServletRequest request, String paramName,
			boolean optional, DateFormat dateFormat) throws ServletException,
			IOException, ServiceException {
		String paramValue = request.getParameter(paramName);
		if (paramValue == null || paramValue.trim().isEmpty()) {
			if (optional)
				return null;
			throw new ServiceException(MSG_PARAM_MISSING + paramName);
		}
		try {
			synchronized (dateFormat) {
				return dateFormat.parse(paramValue.trim());
			}
		} catch (ParseException e) {
			throw new ServiceException(MSG_INV_PARAM_FORMAT + paramName);
		}
	}

	protected PrintWriter getTextPrintWriter(HttpServletResponse response)
			throws UnsupportedEncodingException, IOException {
		response.setContentType("text/plain");
		// response.setCharacterEncoding("UTF-8");
		return response.getWriter();
	}

	protected PrintWriter getHtmlPrintWriter(HttpServletResponse response)
			throws UnsupportedEncodingException, IOException {
		response.setContentType("text/html");
		// response.setCharacterEncoding("UTF-8");
		return response.getWriter();
	}

	protected PrintWriter getCsvPrintWriter(HttpServletResponse response,
			String filename) throws UnsupportedEncodingException, IOException {
		response.setContentType("text/x-comma-separated-values");
		response.setHeader("Content-Disposition", "attachment; filename="
				+ filename);
		return response.getWriter();
	}

	protected String dateToStr(Date date, DateFormat dateFormat) {
		if (date == null)
			return "";
		return dateFormat.format(date);
	}

	protected String objToStr(Object object) {
		if (object == null)
			return "";
		return object.toString();
	}

	protected void logRequestParams(Map<String, String[]> reqMap) {
		if (!logger.isDebugEnabled())
			return;
		if (reqMap == null)
			logger.debug("No request params");
		Iterator<String> itKeys = reqMap.keySet().iterator();
		while (itKeys.hasNext()) {
			String key = itKeys.next();
			String[] values = (String[]) reqMap.get(key);
			StringBuilder sb = new StringBuilder();
			if (values != null)
				for (int i = 0; i < values.length; i++) {
					if (i > 0)
						sb.append(", ");
					sb.append(values[i]);
				}
			logger.debug("Param:" + key + " value=" + sb);
		}
	}

	protected VelocityEngine initVelocity() throws Exception {
		Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class", "org.apache.velocity"
				+ ".runtime.resource.loader.ClasspathResourceLoader");
		VelocityEngine ve = new VelocityEngine();
		ve.init(p);
		return ve;
	}

	protected FunctionFlags getFunctionFlags(DataSource dataSourceAuth,
			String user, String authAppFunction) throws ApplicationException,
			NotAuthorizedException {
		FunctionFlags ff;
		DbAuth dbAuth = new DbAuth("dbauth", dataSourceAuth);
		try {
			dbAuth.connect();
			ff = dbAuth.getFunctionFlag(dbAuth.getFunctionFlagForFunction(
					dbAuth.readUserId(user),
					dbAuth.readFunctionId(authAppFunction)));
		} catch (Exception ex) {
			throw new ApplicationException(
					"[HttpService] Impossibile accedere alla banca"
							+ " dati dell'autenticazione", ex);
		} finally {
			try {
				dbAuth.disconnect();
			} catch (SQLException e) {
				logger.error("Error in disconnect", e);
			}
		}
		if (ff == null)
			throw new NotAuthorizedException("L'utente " + user + " non è "
					+ "autorizzato all'uso di questa applicazione");
		return ff;
	}

	Logger getLogger() {
		return logger;
	}

	void checkRemotePermissions(HttpServletRequest request)
			throws NotAuthorizedException {
		if (setAllowedIP == null)
			return;
		String remoteAddr = request.getRemoteAddr();
		if (!setAllowedIP.contains(remoteAddr)) {
			String msg = "Access denied to not authorized remote host: "
					+ remoteAddr;
			logger.warn(msg);
			throw new NotAuthorizedException(msg);
		}
	}

}
