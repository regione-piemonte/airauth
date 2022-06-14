/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */

package it.csi.util.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Classe base per gli elementi di configurazione basati sul formato parametro =
 * valore.
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.4 $, $Date: 2010/02/15 12:34:24 $
 */
public abstract class ParametricConfigItem extends ConfigItem {

	private static final long serialVersionUID = -2014672590996728522L;
	protected static final int TYPE_INTEGER = 0;
	protected static final int TYPE_INTEGER_LIST = 1;
	protected static final int TYPE_DOUBLE = 2;
	protected static final int TYPE_DOUBLE_LIST = 3;
	protected static final int TYPE_STRING = 4;
	protected static final int TYPE_STRING_LIST = 5;
	protected static final int TYPE_BOOLEAN = 6;
	protected static final int TYPE_BOOLEAN_LIST = 7;
	protected static final int TYPE_DATE = 8;

	private Parameter params[];
	private Object values[];
	boolean defaults;

	protected ParametricConfigItem(Parameter params[]) {
		this.params = params;
		values = new Object[params.length];
		for (int i = 0; i < params.length; i++)
			values[i] = null;
		defaults = false;
	}

	public String getLabel(int paramId) {
		try {
			return (params[paramId].label);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParamIndexOutOfBoundsException("Unknown parameter");
		}
	}

	public synchronized Object getValue(int paramId) {
		try {
			return (values[paramId]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParamIndexOutOfBoundsException("Unknown parameter");
		}
	}

	public synchronized void setValue(int paramId, Object value)
			throws ConfigItemException {
		try {
			typeCheck(paramId, value);
			values[paramId] = validateParameter(paramId, value);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParamIndexOutOfBoundsException("Unknown parameter");
		}
	}

	List dumpConfig() {
		List configLines = new ArrayList();
		for (int i = 0; i < params.length; i++)
			if (values[i] instanceof List) {
				StringBuffer strbuf = new StringBuffer();
				strbuf.append(" ").append(params[i].label).append(" = ");
				ListIterator li = ((List) values[i]).listIterator();
				boolean first = true;
				while (li.hasNext()) {
					if (first)
						first = false;
					else
						strbuf.append(", ");
					strbuf.append(li.next().toString());
				}
				configLines.add(strbuf.toString());
			} else
				configLines.add(" " + params[i].label + " = " + values[i]);
		return (configLines);
	}

	void parseConfig(List configLines) throws ConfigItemException {
		Map mapParameters = new HashMap();
		ListIterator li = configLines.listIterator();

		while (li.hasNext()) {
			String line = (String) li.next();
			int equal_index = line.indexOf('=');
			if (equal_index == -1)
				throw new ConfigItemException("Config line without =");
			String param_name = line.substring(0, equal_index).trim();

			if (param_name.length() == 0)
				throw new ConfigItemException(
						"Config line without parameter name");
			try {
				String param_value = line.substring(equal_index + 1).trim();
				if (param_value.length() == 0)
					throw new ConfigItemException("Parameter " + param_name
							+ " without value");
				if (mapParameters.put(param_name, param_value) != null)
					throw new ConfigItemException("Duplicated parameter: "
							+ param_name);
			} catch (IndexOutOfBoundsException ex) {
				throw new ConfigItemException("Parameter " + param_name
						+ " without value");
			}
		}

		parseParameters(mapParameters);
		return;
	}

	private void parseParameters(Map mapParameters) throws ConfigItemException {
		String missingList = "";
		boolean missing = false;
		defaults = false;

		for (int i = 0; i < params.length; i++)
			values[i] = null;
		for (int i = 0; i < params.length; i++) {
			String label = params[i].label;
			String strvalue = (String) mapParameters.get(label);
			if (strvalue == null) {
				if (params[i].isOptional()) {
					defaults = true;
					/*
					 * TBD: sarebbe meglio assegnare una copia dell'oggetto di
					 * default o rendere l'oggetto immutabile
					 */
					values[i] = validateParameter(i, params[i].defaultValue);
				} else {
					missing = true;
					missingList = missingList + "\n" + label;
				}
				continue;
			}
			switch (params[i].type) {
			case TYPE_INTEGER:
				try {
					values[i] = validateParameter(i, Integer.valueOf(strvalue));
				} catch (NumberFormatException ex) {
					throw new ConfigItemException(
							"Invalid format for parameter " + label);
				}
				break;
			case TYPE_DOUBLE:
				try {
					values[i] = validateParameter(i, Double.valueOf(strvalue));
				} catch (NumberFormatException ex) {
					throw new ConfigItemException(
							"Invalid format for parameter " + label);
				}
				break;
			case TYPE_BOOLEAN:
				try {
					values[i] = validateParameter(i, Boolean.valueOf(strvalue));
				} catch (NumberFormatException ex) {
					throw new ConfigItemException(
							"Invalid format for parameter " + label);
				}
				break;
			case TYPE_STRING:
				values[i] = validateParameter(i, strvalue);
				break;
			case TYPE_DATE:
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
					Date date = sdf.parse(strvalue);
					values[i] = validateParameter(i, date);
				} catch (ParseException ex) {
					throw new ConfigItemException(
							"Invalid format for parameter " + label);
				}
				break;
			case TYPE_INTEGER_LIST:
				List items = new ArrayList();
				StringTokenizer strtok = new StringTokenizer(strvalue, ",");
				while (strtok.hasMoreElements()) {
					try {
						items.add(Integer
								.valueOf((String) strtok.nextElement()));
					} catch (NumberFormatException ex) {
						throw new ConfigItemException(
								"Invalid format for parameter " + label);
					}
				}
				values[i] = validateParameter(i, items);
				break;
			case TYPE_DOUBLE_LIST:
				items = new ArrayList();
				strtok = new StringTokenizer(strvalue, ",");
				while (strtok.hasMoreElements()) {
					try {
						items
								.add(Double.valueOf((String) strtok
										.nextElement()));
					} catch (NumberFormatException ex) {
						throw new ConfigItemException(
								"Invalid format for parameter " + label);
					}
				}
				values[i] = validateParameter(i, items);
				break;
			case TYPE_BOOLEAN_LIST:
				items = new ArrayList();
				strtok = new StringTokenizer(strvalue, ",");
				while (strtok.hasMoreElements()) {
					try {
						items.add(Boolean
								.valueOf((String) strtok.nextElement()));
					} catch (NumberFormatException ex) {
						throw new ConfigItemException(
								"Invalid format for parameter " + label);
					}
				}
				values[i] = validateParameter(i, items);
				break;
			case TYPE_STRING_LIST:
				items = new ArrayList();
				strtok = new StringTokenizer(strvalue, ",");
				while (strtok.hasMoreElements()) {
					items.add(strtok.nextElement());
				}
				values[i] = validateParameter(i, items);
				break;
			default:
				throw new RuntimeException("Unsupported parameter type");
			}
		}
		if (missing)
			throw new ConfigItemException("Needed parameter(s) missing: "
					+ missingList);

		return;
	}

	private void typeCheck(int paramId, Object value)
			throws ConfigItemException {
		if (value == null)
			throw new ConfigItemException("Null parameter not accepted");
		Class itemClass = null;
		switch (params[paramId].type) {
		case TYPE_INTEGER:
			if (value instanceof Integer)
				return;
			break;
		case TYPE_DOUBLE:
			if (value instanceof Double)
				return;
			break;
		case TYPE_BOOLEAN:
			if (value instanceof Boolean)
				return;
			break;
		case TYPE_STRING:
			if (value instanceof String)
				return;
			break;
		case TYPE_DATE:
			if (value instanceof Date)
				return;
			break;
		case TYPE_INTEGER_LIST:
			itemClass = Integer.TYPE;
		case TYPE_DOUBLE_LIST:
			itemClass = Double.TYPE;
		case TYPE_BOOLEAN_LIST:
			itemClass = Boolean.TYPE;
		case TYPE_STRING_LIST:
			itemClass = "".getClass();
			if (value instanceof List) {
				boolean ok = true;
				ListIterator li = ((List) value).listIterator();
				while (li.hasNext()) {
					if (!(itemClass.isInstance(li.next())))
						ok = false;
				}
				if (ok)
					return;
			}
			break;
		default:
			throw new RuntimeException("Unsupported parameter type");
		}
		throw new ConfigItemException("Parameter type mismatch");
	}

	protected Object validateParameter(int paramId, Object value) {
		return (value);
	}

	public synchronized boolean isValid() {
		for (int i = 0; i < values.length; i++)
			if (values[i] == null)
				return (false);
		return (true);
	}

	public synchronized boolean hasDefaults() {
		return (defaults);
	}

	public synchronized String toString() {
		StringBuffer strbuff = new StringBuffer();
		strbuff.append(getClass().getName());
		strbuff.append(" [");
		for (int i = 0; i < params.length; i++) {
			if (i > 0)
				strbuff.append(", ");
			strbuff.append(params[i].label);
			strbuff.append("=");
			strbuff.append(values[i]);
		}
		strbuff.append("]");
		return (strbuff.toString());
	}

}
