package com.betera.logviewer.file.column;

public class LogfileColumn
{

    private String name;
    private String content;

    public LogfileColumn(String aName, String aContent)
    {
        setName(aName);
        setContent(aContent);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String toString()
    {
        return getContent();
    }

}
