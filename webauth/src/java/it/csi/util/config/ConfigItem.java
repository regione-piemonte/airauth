/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */

package it.csi.util.config;

import java.io.*;
import java.util.*;

/**
 * Classe base per gli elementi di configurazione.
 *
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $, $Date: 2002/09/02 08:19:21 $
 */
public abstract class ConfigItem
    implements Serializable
{
    private String tagBegin;
    private String tagEnd;

    public ConfigItem()
    {
	String tmp = getClass().getName();
	int dot_index = tmp.lastIndexOf('.');
	if (dot_index != -1)
	    tmp = tmp.substring(dot_index+1);
	tagBegin = "<" + tmp + ">";
	tagEnd = "</" + tmp + ">";
    }

    // Implement as synchronized!
    abstract public boolean isValid();
    
    // Implement as synchronized!
    abstract public boolean hasDefaults();
    
    public synchronized void dumpToCharStream(Writer wr)
	throws IOException, ConfigItemException
    {
	if (!isValid())
	    throw new ConfigItemException("Invalid item cannot be saved");
	wr.write(tagBegin);
	wr.write('\n');
	ListIterator li = dumpConfig().listIterator();
	while (li.hasNext()) {
	    wr.write((String)li.next());
	    wr.write('\n');
	}
	wr.write(tagEnd);
	wr.write('\n');
	return;
    }
 
    abstract List dumpConfig();

    public synchronized boolean initFromCharStream(Reader rd)
	throws IOException, ConfigException, ConfigItemException, 
	EndOfConfigException
    {
	boolean expectBeginTag = true;
	List configLines = new ArrayList();
	
	LineNumberReader lnrd;
	if (rd instanceof LineNumberReader)
	    lnrd = (LineNumberReader)rd;
	else
	    lnrd = new LineNumberReader(rd);
	String line;
	while (true) {
	    line = lnrd.readLine();
	    if (line == null) {
		if (expectBeginTag)
		    throw new EndOfConfigException("End of stream reached");
		else
		    throw new ConfigException("Unexpected end of stream");
	    }
	    line = line.trim();
	    if (line.length() == 0)
		continue;
	    if (line.charAt(0) == '#')
		continue;

	    if (expectBeginTag) {
		if (line.charAt(0) != '<')
		    throw new ConfigException("Begin tag expected, line=" +
			lnrd.getLineNumber());
		if (!line.equals(tagBegin))
		    throw new ConfigException("Begin tag mismatch, line=" +
			lnrd.getLineNumber());
		expectBeginTag = false;
		continue;
	    }

	    if (line.charAt(0) == '<') {
		if (!line.equals(tagEnd))
		    throw new ConfigException("End tag mismatch, line=" +
			lnrd.getLineNumber());
		break;
	    }

	    configLines.add(line);
	}
	
	parseConfig(configLines);
	
	return(hasDefaults());
    }

    abstract void parseConfig(List configLines)
	throws ConfigItemException;

    public List getListFromReader(Reader rd)
	throws IOException, ConfigException, ConfigItemException,
	       InstantiationException, IllegalAccessException
    {
	List list_items = new ArrayList();
	LineNumberReader lnrd;
	if (rd instanceof LineNumberReader)
	    lnrd = (LineNumberReader)rd;
	else
	    lnrd = new LineNumberReader(rd);
	try {
	    while (true) {
		ConfigItem cfgItem = (ConfigItem)getClass().newInstance();
		cfgItem.initFromCharStream(lnrd);
		list_items.add(cfgItem);
	    }
	}
	catch (EndOfConfigException ex) {
	}
	return(list_items);
    }
}


