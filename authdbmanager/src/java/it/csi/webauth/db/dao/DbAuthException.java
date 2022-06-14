/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.dao;

/**
 * Eccezione base per gli errori negli algoritmi di interfacciamento al
 * data base aria.
 *
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */
public class DbAuthException extends Exception
{
    private static final long serialVersionUID = 2833723943219196552L;
    public DbAuthException()
    {
	super();
    }
    public DbAuthException(String description)
    {
	super(description);
    }
}

