package com.betera.logviewer.file.column.parser;

import com.betera.logviewer.file.column.LogfileColumnConfig;
import com.betera.logviewer.file.column.LogfileColumnParser;

public class LogfileColumnNoneParser
        implements LogfileColumnParser
{
    @Override
    public String parse(String aLine, LogfileColumnConfig entry)
    {
        String ret = aLine;

        if ( ret.length() > entry.getMaxColumnSize() )
        {
            ret = ret.substring(0, entry.getMaxColumnSize());
        }

        return ret;
    }

    @Override
    public String getName()
    {
        return "NONE";
    }

    @Override
    public String getDescription()
    {
        return "No delimiter used. This will return the whole text as line.";
    }
}
