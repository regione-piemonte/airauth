/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */

package it.csi.util.config;

/**
 * Eccezione per segnalare l'accesso ad un parametro non esistente in un
 * ParametricConfigItem
 *
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.2 $, $Date: 2005/07/07 14:16:53 $
 */
public class ParamIndexOutOfBoundsException extends RuntimeException
{
    private static final long serialVersionUID = 3789476832960760039L;

    public ParamIndexOutOfBoundsException(String description)
    {
	super(description);
    }
}

