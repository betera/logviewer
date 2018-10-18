package com.betera.logviewer.file.column.parser;

import com.betera.logviewer.file.column.LogfileColumnConfigEntry;
import com.betera.logviewer.file.column.LogfileColumnParser;

public class LogfileColumnLengthParser
        implements LogfileColumnParser
{
    @Override
    public String parse(String aLine, LogfileColumnConfigEntry entry)
    {
        String ret = aLine;
        int length = Integer.valueOf(entry.getParams()[0]);
        if ( length <= aLine.length() )
        {
            ret = aLine.substring(0, length);
        }

        if ( ret.length() > entry.getMaxColumnSize() )
        {
            ret = ret.substring(0, entry.getMaxColumnSize());
        }

        return ret;
    }
}
