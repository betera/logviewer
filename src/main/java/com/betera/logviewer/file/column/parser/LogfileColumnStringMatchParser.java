package com.betera.logviewer.file.column.parser;

import com.betera.logviewer.file.column.LogfileColumnParser;

public class LogfileColumnStringMatchParser
        implements LogfileColumnParser
{

    @Override
    public String parse(String aLine, String... params)
    {
        String string = params[0];
        if ( string.equals("SPACE") )
        {
            string = " ";
        }

        int index = aLine.indexOf(string);
        if ( index >= 0 )
        {
            return aLine.substring(0, index + 1);
        }
        return aLine;
    }
}
