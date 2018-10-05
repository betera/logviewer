package com.betera.logviewer.file.column;

public interface LogfileColumnParser
{

    String parse(String aLine, String... params);

}
