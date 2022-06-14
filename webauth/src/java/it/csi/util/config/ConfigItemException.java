/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */

package it.csi.util.config;

/**
 * Eccezione per gli errori all'interno di un item di configurazione.
 *
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.2 $, $Date: 2005/07/07 14:16:53 $
 */
public class ConfigItemException extends ConfigException
{
    private static final long serialVersionUID = 194057801216830641L;

    public ConfigItemException(String description)
    {
	super(description);
    }
}

