package com.betera.logviewer.file.column.parser;

import com.betera.logviewer.file.column.LogfileColumnConfig;
import com.betera.logviewer.file.column.LogfileColumnParser;

public class LogfileColumnLengthParser
        implements LogfileColumnParser
{
    @Override
    public String parse(String aLine, LogfileColumnConfig entry)
    {
        String ret = aLine;
        int length = Integer.valueOf(entry.getParams()[0]);
        if ( length <= aLine.length() )
        {
            ret = aLine.substring(0, length);
        }

        if ( entry.getMaxColumnSize() > 0 && ret.length() > entry.getMaxColumnSize() )
        {
            ret = ret.substring(0, entry.getMaxColumnSize());
        }

        return ret;
    }

    @Override
    public String getName()
    {
        return "LENGTH";
    }

    @Override
    public String getDescription()
    {
        return "Delimits the column by a fixed integer length as first and only parameter";
    }
}
