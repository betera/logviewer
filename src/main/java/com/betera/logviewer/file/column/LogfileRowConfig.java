package com.betera.logviewer.file.column;

import com.betera.logviewer.Util;
import java.util.ArrayList;
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

    public List<String> getToIgnore()
    {
        return toIgnore;
    }

    public void setToIgnore(List<String> toIgnore)
    {
        this.toIgnore = toIgnore;
    }

    public boolean isInitiallyHidden()
    {
        return initiallyHidden;
    }

    public void setInitiallyHidden(boolean initiallyHidden)
    {
        this.initiallyHidden = initiallyHidden;
    }

    public List<String> getEffectiveIgnoreList()
    {
        List<String> effList = new ArrayList<>();
        for ( String str : getIgnoreList() )
        {
            if ( Util.containsOnlySpaces(str) )
            {
                effList.add(str.length() + "");
            }
            else
            {
                effList.add(str.replace("\t", "\\t"));
            }
        }
        return effList;
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

    public LogfileRowConfig copy()
    {
        List<LogfileColumnConfig> entriesCopy = new ArrayList<>();
        for ( LogfileColumnConfig colConfig : entries )
        {
            entriesCopy.add(colConfig.copy());
        }
        return new LogfileRowConfig(name,
                                    matcher,
                                    (List<String>) ((ArrayList) toIgnore).clone(),
                                    entriesCopy.toArray(new LogfileColumnConfig[entriesCopy.size()]));
    }

}
