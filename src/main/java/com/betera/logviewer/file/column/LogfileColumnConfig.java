package com.betera.logviewer.file.column;

import com.betera.logviewer.LogViewer;

public class LogfileColumnConfig
{

    private String columnName;
    private String entryType;
    private String[] params;
    private int maxColumnSize;
    private boolean initiallyHidden;

    public LogfileColumnConfig(String aColumnName,
                               String entryType,
                               int maxColumnSize,
                               boolean initiallyHidden,
                               String... params)
    {
        setColumnName(aColumnName);
        setEntryType(entryType);
        setInitiallyHidden(initiallyHidden);
        setMaxColumnSize(maxColumnSize);
        setParams(params);
    }

    public int getMaxColumnSize()
    {
        return maxColumnSize > 0 ? maxColumnSize : 99999;
    }

    public void setMaxColumnSize(int maxColumnSize)
    {
        this.maxColumnSize = maxColumnSize;
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
