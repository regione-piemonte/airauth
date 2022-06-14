/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.service;

import it.csi.webauth.db.dao.DbAuth;
import it.csi.webauth.db.dao.DbAuthException;
import it.csi.webauth.db.dao.DbAuthNotFoundException;
import it.csi.webauth.db.model.FunctionFlags;
import it.csi.webauth.db.model.Utente;
import it.csi.webauth.service.ServiceError.Code;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

@Path("/dbauth")
public class AuthWebService extends WebService {

	private static final String VERSION = "1.0.0";
	private static final String DS_NAME_AUTH = "dbAuth";
	private static Logger lg = Logger.getLogger("authservice." + AuthWebService.class.getSimpleName());

	private DataSource dataSourceAuth;

	/**
	 * @param servletContext
	 */
	public AuthWebService(@Context ServletContext servletContext) {
		lg.info("Initializing Web Auth Service, version " + VERSION);
		String dsNameAuth = getInitParam(servletContext, "dataSourceAuth", DS_NAME_AUTH);
		lg.info("Getting datasource " + dsNameAuth + "...");
		try {
			dataSourceAuth = (DataSource) new InitialContext().lookup("java:comp/env/" + dsNameAuth);
		} catch (NamingException ex) {
			lg.error("Cannot get dataSource " + dsNameAuth + ": ", ex);
		}
	}

	/**
	 * @param context
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	private String getInitParam(ServletContext context, String name, String defaultValue) {
		String value = context.getInitParameter(name);
		if (value == null)
			value = defaultValue;

		return value;
	}

	protected Response runTask(ServiceTask task, boolean write) {
		DbAuth db = null;
		try {
			db = new DbAuth("dbauth", dataSourceAuth, write);
			db.connect();
			Object result = task.execute(db);
			return result != null ? Response.ok(result).build() : Response.noContent().build();
		} catch (InvalidParamException e) {
			lg.debug(e.getMessage(), e);
			return Response.status(Status.BAD_REQUEST).entity(new ServiceError(Code.INVALID_PARAM, e)).build();
		} catch (DbAuthNotFoundException e) {
			lg.debug(e.getMessage(), e);
			return Response.status(Status.NOT_FOUND).entity(new ServiceError(Code.RESOURCE_NOT_FOUND, e)).build();
		} catch (DbAuthException e) {
			lg.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ServiceError(Code.DATA_BASE_ERROR, e))
					.build();
		} catch (SQLException e) {
			lg.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ServiceError(Code.DATA_BASE_ERROR, e))
					.build();
		} catch (IOException e) {
			lg.error(e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ServiceError(Code.DATA_BASE_ERROR, e))
					.build();
		} finally {
			if (db != null) {
				try {
					db.disconnect();
				} catch (SQLException ex) {
					lg.error(ex);
				}
			}
		}
	}

	/**
	 * @param nameFilter
	 * @return
	 */
	@GET
	@Path("/userlist")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserList(@QueryParam("namefilter") final String nameFilter) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				return db.readUserList(nameFilter);
			}
		});
	}

	/**
	 * @param id
	 * @return
	 */
	@GET
	@Path("/users/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("id") final String id) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				return db.readUser(param2Integer(id, "user id"));
			}
		});
	}

	/**
	 * @param username
	 * @return
	 */
	@GET
	@Path("/usersbyname/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserByName(@PathParam("username") final String username) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				return db.readUser(db.readUserId(username));
			}
		});
	}

	/**
	 * @param user
	 * @param password
	 * @param method
	 * @return
	 */
	@GET
	@Path("/authentication/{user}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response isAuthenticated(@PathParam("user") final String user, @PathParam("password") final String password,
			@QueryParam("method") final String method) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				Integer userId = db.readUserId(user);
				Utente userInfo = db.readUser(userId);
				String encryptedPwd = userInfo.getPassword();
				try {
					return DbAuth.isAuthorizedPasswordForApache(encryptedPwd.substring(0, 2), password, encryptedPwd)
							? userId
							: null;
				} catch (Exception e) {
					throw new DbAuthException("Password check failed: " + e.getMessage());
				}
			}
		});
	}

	/**
	 * @param function
	 * @param userId
	 * @return
	 */
	@GET
	@Path("/functionflags/{function}/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFunctionFlags(@PathParam("function") final String function,
			@PathParam("userId") final String userId) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				Integer functionId = db.readFunctionId(function);
				List<FunctionFlags> ffList = db.getFunctionFlagForFunction(param2Integer(userId, "user id"),
						functionId);
				return db.getFunctionFlag(ffList);
			}
		});
	}

	/**
	 * @param function
	 * @param userName
	 * @return
	 */
	@GET
	@Path("/domainfunctionflags/{function}/{userName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDomainFunctionFlagsMap(@PathParam("function") final String function,
			@PathParam("userName") final String userName) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				return db.getPermissionMap(userName, function);
			}
		});
	}

	/**
	 * @param objectTypeId
	 * @return
	 */
	@GET
	@Path("/domainacls/{objectTypeId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDomainAcls(@PathParam("objectTypeId") final String objectTypeId) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				return db.readDomainAclListFromObjectType(param2Integer(objectTypeId, "object type id"));
			}
		});
	}

	/**
	 * @param objectTypeId
	 * @param domainId
	 * @return
	 */
	@GET
	@Path("/domainacls/{objectTypeId}/{domainId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDomainAcls(@PathParam("objectTypeId") final String objectTypeId,
			@PathParam("domainId") final String domainId) {
		return runTask(new ServiceTask() {
			@Override
			public Object execute(DbAuth db) throws InvalidParamException, DbAuthException, SQLException {
				return db.readDomainAcl(param2Integer(domainId, "domain id"),
						param2Integer(objectTypeId, "object type id"));
			}
		});
	}

}