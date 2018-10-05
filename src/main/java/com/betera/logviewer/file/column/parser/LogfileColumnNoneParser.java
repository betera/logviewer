package com.betera.logviewer.file.column.parser;

import com.betera.logviewer.file.column.LogfileColumnParser;

public class LogfileColumnNoneParser
        implements LogfileColumnParser
{
    @Override
    public String parse(String aLine, String... params)
    {
        return aLine;
    }
}
