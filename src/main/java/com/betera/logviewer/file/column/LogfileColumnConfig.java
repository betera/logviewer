package com.betera.logviewer.file.column;

import java.util.List;

public class LogfileColumnConfig
{

    private LogfileColumnConfigEntry[] entries;
    private String matcher;
    private String name;
    private List<String> toIgnore;
    private boolean initiallyHidden;

    public LogfileColumnConfig(String aName,
                               String aMatcher,
                               List<String> toIgnore,
                               LogfileColumnConfigEntry... columnEntries)
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

    public LogfileColumnConfigEntry[] getEntries()
    {
        return entries;
    }

    public void setEntries(LogfileColumnConfigEntry[] entries)
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
