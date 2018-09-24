package com.betera.logviewer.file.column;

public abstract class AbstractLogfileColumn
        implements LogfileColumn
{
    private String content;

    public AbstractLogfileColumn(String line)
    {
        content = line;
    }

    @Override
    public String getContent()
    {
        return content;
    }
}
