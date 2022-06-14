/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.service;

import java.util.Date;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

public abstract class WebService extends Application {

	// private static Logger lg = Logger.getLogger("authservice."
	// + WebService.class.getSimpleName());

	public WebService() {
	}

	protected Response runTask(ServiceTask task) {
		return runTask(task, false);
	}

	abstract protected Response runTask(ServiceTask task, boolean write);

	/**
	 * @param param
	 * @param description
	 * @return
	 * @throws InvalidParamException
	 */
	protected int param2int(String param, String description)
			throws InvalidParamException {
		try {
			return Integer.parseInt(param);
		} catch (NumberFormatException e) {
			throw new InvalidParamException("Unparseable " + description + " '"
					+ param + "'", e);
		}
	}

	/**
	 * @param param
	 * @param description
	 * @return
	 * @throws InvalidParamException
	 */
	protected short param2short(String param, String description)
			throws InvalidParamException {
		try {
			return Short.parseShort(param);
		} catch (NumberFormatException e) {
			throw new InvalidParamException("Unparseable " + description + " '"
					+ param + "'", e);
		}
	}

	/**
	 * @param param
	 * @param description
	 * @return
	 * @throws InvalidParamException
	 */
	protected byte param2byte(String param, String description)
			throws InvalidParamException {
		try {
			return Byte.parseByte(param);
		} catch (NumberFormatException e) {
			throw new InvalidParamException("Unparseable " + description + " '"
					+ param + "'", e);
		}
	}

	/**
	 * @param param
	 * @param description
	 * @return
	 * @throws InvalidParamException
	 */
	protected Integer param2Integer(String param, String description)
			throws InvalidParamException {
		try {
			if (param == null || param.isEmpty())
				return null;
			return Integer.parseInt(param.trim());
		} catch (NumberFormatException e) {
			throw new InvalidParamException("Unparseable " + description + " '"
					+ param + "'", e);
		}
	}

	/**
	 * @param param
	 * @param description
	 * @return
	 * @throws InvalidParamException
	 */
	protected Boolean param2Boolean(String param, String description)
			throws InvalidParamException {
		try {
			if (param == null || param.isEmpty())
				return null;
			return Boolean.parseBoolean(param.trim());
		} catch (NumberFormatException e) {
			throw new InvalidParamException("Unparseable " + description + " '"
					+ param + "'", e);
		}
	}

	/**
	 * @param param
	 * @param description
	 * @return
	 * @throws InvalidParamException
	 */
	protected Date param2Date(String param, String description)
			throws InvalidParamException {
		try {
			if (param == null || param.isEmpty() || "null".equals(param))
				return null;
			return new Date(Long.parseLong(param));
		} catch (NumberFormatException e) {
			throw new InvalidParamException("Unparseable " + description + " '"
					+ param + "'", e);
		}
	}

}