package com.betera.logviewer.file.column;

import com.betera.logviewer.file.column.parser.LogfileColumnLengthParser;
import com.betera.logviewer.file.column.parser.LogfileColumnNoneParser;
import com.betera.logviewer.file.column.parser.LogfileColumnStringMatchParser;

public enum LogfileColumnParseType
{
    STRING(LogfileColumnStringMatchParser.class), LENGTH(LogfileColumnLengthParser.class), NONE(LogfileColumnNoneParser.class);

    private Class<? extends LogfileColumnParser> parserClass;

    LogfileColumnParseType(Class<? extends LogfileColumnParser> parserClass)
    {
        this.parserClass = parserClass;
    }

    public Class<? extends LogfileColumnParser> getParserClass()
    {
        return parserClass;
    }
}
