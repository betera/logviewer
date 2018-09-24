package com.betera.logviewer.file.column;

public interface LogfileColumn
{

    boolean matches(String line);

    String getContent();

}
