package com.betera.logviewer.file.column.parser;

import com.betera.logviewer.file.column.LogfileColumnConfigEntry;
import com.betera.logviewer.file.column.LogfileColumnParser;

public class LogfileColumnStringMatchParser
        implements LogfileColumnParser
{

    @Override
    public String parse(String aLine, LogfileColumnConfigEntry entry)
    {
        String string = entry.getParams()[0];
        if ( string.equals("SPACE") )
        {
            string = " ";
        }

        int index = aLine.indexOf(string);
        String ret = aLine;
        if ( index >= 0 )
        {
            ret = aLine.substring(0, index + 1);
        }
        if ( ret.length() > entry.getMaxColumnSize() )
        {
            ret = ret.substring(0, entry.getMaxColumnSize());
        }

        return ret;
    }
}
