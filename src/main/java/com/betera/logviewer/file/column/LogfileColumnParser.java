package com.betera.logviewer.file.column;

public interface LogfileColumnParser
{

    String parse(String aLine, LogfileColumnConfigEntry entry);

}
