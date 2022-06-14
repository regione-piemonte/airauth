/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Funzioni di utilita' per WebAuth
 * 
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */

public class WAUtil {
	public static final String REVISION = "$Revision: 1.1 $";

	public static String print(Object obj) {
		if (obj == null)
			return "";
		return obj.toString();
	}

	public static Funzione getFunzione(List<Funzione> list_funzioni,
			Integer id_funzione) {
		if (list_funzioni == null || id_funzione == null)
			return null;
		Iterator<Funzione> it = list_funzioni.iterator();
		while (it.hasNext()) {
			Funzione fn = it.next();
			if (id_funzione.equals(fn.getIdFunzione()))
				return fn;
		}
		return null;
	}

	public static Ambito getAmbito(List<Ambito> list_ambiti, Integer id_ambito) {
		if (list_ambiti == null || id_ambito == null)
			return null;
		Iterator<Ambito> it = list_ambiti.iterator();
		while (it.hasNext()) {
			Ambito ambito = it.next();
			if (id_ambito.equals(ambito.getIdAmbito()))
				return ambito;
		}
		return null;
	}

	public static List<FunzioneGruppo> orderGroupFunctions(
			List<Funzione> list_functions, List<FunzioneGruppo> list_grfuns) {
		if (list_functions == null || list_functions.size() < 2)
			return list_grfuns;
		if (list_grfuns == null || list_grfuns.size() < 2)
			return list_grfuns;

		List<FunzioneGruppo> ordered_list_grfuns = new ArrayList<FunzioneGruppo>();
		Iterator<Funzione> li_fn = list_functions.iterator();
		while (li_fn.hasNext()) {
			Funzione fn = li_fn.next();
			Integer id_fn = fn.getIdFunzione();
			if (id_fn == null)
				continue;
			Iterator<FunzioneGruppo> li_grfn = list_grfuns.iterator();
			while (li_grfn.hasNext()) {
				FunzioneGruppo fg = li_grfn.next();
				if (id_fn.equals(fg.getIdFunzione())) {
					li_grfn.remove();
					ordered_list_grfuns.add(fg);
				}
			}
		}
		ordered_list_grfuns.addAll(list_grfuns);
		list_grfuns.clear(); // TODO: sarebbe da togliere, verificare dove viene
		// chiamata la funzione
		return ordered_list_grfuns;
	}

}
