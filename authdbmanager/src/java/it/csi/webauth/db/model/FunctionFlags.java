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

public class FunctionFlags
{
    private boolean write_flag;
    private boolean advanced_flag;

    public FunctionFlags()
    {
        this(false, false);
    }

    public FunctionFlags(boolean write_flag, boolean advanced_flag)
    {
        this.write_flag = write_flag;
        this.advanced_flag = advanced_flag;
    }

    public boolean getWriteFlag()
    {
        return(write_flag);
    }

    public boolean getAdvancedFlag()
    {
        return(advanced_flag);
    }

    public void setWriteFlag(boolean write_flag)
    {
        this.write_flag = write_flag;
    }

    public void setAdvancedFlag(boolean advanced_flag)
    {
        this.advanced_flag = advanced_flag;
    }
    
    public void orWriteFlag(boolean flag)
    {
        this.write_flag |= flag;
    }

    public void orAdvancedFlag(boolean flag)
    {
        this.advanced_flag |= flag;
    }

    public void orWriteFlag(Boolean flag)
    {
        if (flag != null)
            this.write_flag |= flag.booleanValue();
    }

    public void orAdvancedFlag(Boolean flag)
    {
        if (flag != null)
            this.advanced_flag |= flag.booleanValue();
    }

}
