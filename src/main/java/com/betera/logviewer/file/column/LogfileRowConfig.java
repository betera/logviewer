package com.betera.logviewer.file.column;

import java.util.List;

public class LogfileRowConfig
{

    private LogfileColumnConfig[] entries;
    private String matcher;
    private String name;
    private List<String> toIgnore;
    private boolean initiallyHidden;

    public LogfileRowConfig(String aName, String aMatcher, List<String> toIgnore, LogfileColumnConfig... columnEntries)
    {
        this.toIgnore = toIgnore;
        this.initiallyHidden = initiallyHidden;
        setName(aName);
        setMatcher(aMatcher);
        setEntries(columnEntries);
    }

    public List<String> getIgnoreList()
    {
        return toIgnore;
    }

    public LogfileColumnConfig[] getEntries()
    {
        return entries;
    }

    public void setEntries(LogfileColumnConfig[] entries)
    {
        this.entries = entries;
    }

    public String getMatcher()
    {
        return matcher;
    }

    public void setMatcher(String matcher)
    {
        this.matcher = matcher;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
