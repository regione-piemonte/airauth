/*
 *Copyright Regione Piemonte - 2022
 *SPDX-License-Identifier: EUPL-1.2-or-later
 */
package it.csi.webauth.db.model;

/**
 * 
 *
 * @author Pierfrancesco.Vallosio@consulenti.csi.it
 * @version $Revision: 1.1 $
 */

public class Record
{
    public static final int UNCHANGED = 0;
    public static final int NEW = 1;
    public static final int DELETED = 2;
    public static final int MODIFIED = 3;

    private int status = UNCHANGED;
    private boolean selected = false;
    private Object usrObj = null;

    public int setUnchanged()
    {
        int oldStatus = status;
        status = UNCHANGED;
        return(oldStatus);
    }

    public int setNew()
    {
        int oldStatus = status;
        status = NEW;
        return(oldStatus);
    }

    public int setDeleted()
    {
        int oldStatus = status;
        status = DELETED;
        return(oldStatus);
    }

    public int setModified()
    {
        int oldStatus = status;
        status = MODIFIED;
        return(oldStatus);
    }

    public int getStatus()
    {
        return(status);
    }

    public boolean getSelected()
    {
        return(selected);
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public Object getUserObject()
    {
        return(usrObj);
    }

    public void setUserObject(Object usrObj)
    {
        this.usrObj = usrObj;
    }
}
