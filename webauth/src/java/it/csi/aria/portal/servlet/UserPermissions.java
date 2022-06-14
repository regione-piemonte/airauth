/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.portal.servlet;

import it.csi.webauth.db.model.FunctionFlags;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
class UserPermissions {
	// TODO: migliorare...
	FunctionFlags ff_useradmin = null;
	FunctionFlags ff_monitor = null;
	FunctionFlags ff_maingui = null;
	FunctionFlags ff_acquisition = null;
	FunctionFlags ff_dbloader = null;
	FunctionFlags ff_import_export = null;
	FunctionFlags ff_netmanager = null;
	FunctionFlags ff_sw_spec = null;
}
