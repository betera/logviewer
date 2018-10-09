package com.betera.logviewer.file.column;

import com.betera.logviewer.LogViewer;

public class LogfileColumnConfigEntry
{

    private String columnName;
    private String entryType;
    private String[] params;
    private boolean initiallyHidden;

    public LogfileColumnConfigEntry(String aColumnName, String entryType, boolean initiallyHidden, String... params)
    {
        setColumnName(aColumnName);
        setEntryType(entryType);
        setInitiallyHidden(initiallyHidden);
        setParams(params);
    }

    public boolean isInitiallyHidden()
    {
        return initiallyHidden;
    }

    public void setInitiallyHidden(boolean initiallyHidden)
    {
        this.initiallyHidden = initiallyHidden;
    }

    public LogfileColumnParser getParser()
    {
        LogfileColumnParseType parser = LogfileColumnParseType.valueOf(entryType);
        try
        {
            return parser.getParserClass().newInstance();
        }
        catch ( InstantiationException | IllegalAccessException e )
        {
            LogViewer.handleException(e);
        }
        return null;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }

    public String getEntryType()
    {
        return entryType;
    }

    public void setEntryType(String entryType)
    {
        this.entryType = entryType;
    }

    public String[] getParams()
    {
        return params;
    }

    public void setParams(String[] params)
    {
        this.params = params;
    }

    public String toString()
    {
        return getColumnName();
    }

}
