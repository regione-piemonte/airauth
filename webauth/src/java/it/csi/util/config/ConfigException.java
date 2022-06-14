/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */

package it.csi.util.config;

/**
 * Eccezione per gli errori di configurazione.
 *
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.2 $, $Date: 2005/07/07 14:16:53 $
 */
public class ConfigException extends Exception
{
    private static final long serialVersionUID = -1755267570096404551L;

    public ConfigException(String description)
    {
	super(description);
    }
}

