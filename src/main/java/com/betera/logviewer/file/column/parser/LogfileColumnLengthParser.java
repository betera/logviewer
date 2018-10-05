package com.betera.logviewer.file.column.parser;

import com.betera.logviewer.file.column.LogfileColumnParser;

public class LogfileColumnLengthParser
        implements LogfileColumnParser
{
    @Override
    public String parse(String aLine, String... params)
    {
        int length = Integer.valueOf(params[0]);
        if ( length <= aLine.length() )
        {
            return aLine.substring(0, length);
        }
        else
        {
            return aLine;
        }
    }
}
