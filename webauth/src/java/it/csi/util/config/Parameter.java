/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.util.config;

/**
 * Classe che rappresenta un parametro.
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.3 $, $Date: 2010/02/15 11:39:07 $
 */

public class Parameter {
	final String label;
	final int type;
	final Object defaultValue;

	public Parameter(String label, int type) {
		this(label, type, null);
	}

	public Parameter(String label, int type, Object defaultValue) {
		this.label = label;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	boolean isOptional() {
		return (defaultValue != null);
	}
}
