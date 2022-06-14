/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.aria.util.servlet;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author pierfrancesco.vallosio@consulenti.csi.it
 * 
 */
public interface PostDataSender {

	public void send(PrintWriter pw) throws IOException, ApplicationException;

}
