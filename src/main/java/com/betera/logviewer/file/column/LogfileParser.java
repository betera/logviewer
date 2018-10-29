package com.betera.logviewer.file.column;

import com.betera.logviewer.LogViewer;
import com.betera.logviewer.Util;
import com.betera.logviewer.Util.StringReadResult;
import com.betera.logviewer.file.Logfile;
import com.betera.logviewer.ui.edit.ConfigDialog;
import com.betera.logviewer.ui.edit.ConfigEditUIProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogfileParser
        implements ConfigEditUIProvider
{

    private static final LogfileParser instance = new LogfileParser();
    private Map<String, LogfileRowConfig> configs;
    private LogfileParserEditPanel editPanel;

    public static synchronized LogfileParser getInstance()
    {
        return instance;
    }

    public List<LogfileRowConfig> getRowConfigs()
    {
        return new ArrayList<>(configs.values());
    }

    public void readColumnFormatterConfig()
    {
        if ( configs == null )
        {
            configs = new HashMap<>();
        }

        try
        {
            BufferedReader in = new BufferedReader(new FileReader("columnFormatter.config"));

            String line = in.readLine();
            while ( line != null )
            {
                if ( line.startsWith("[") )
                {
                    line = line.substring(1);
                    String fileName = line.split("=")[0];
                    String filePattern = line.split("=")[1];
                    filePattern = filePattern.substring(0, filePattern.length() - 1);

                    List<LogfileColumnConfig> entries = new ArrayList<>();

                    line = in.readLine();
                    List<String> toIgnore = new ArrayList<>();
                    while ( line != null && !line.startsWith("[") )
                    {
                        StringReadResult columnNameRead = Util.readString(line);
                        String columnName = columnNameRead.getValue();
                        line = line.substring(columnNameRead.getReadLength() + 1);

                        if ( columnName.equals("IGNORE") )
                        {
                            String ignoreParam = line;

                            toIgnore.add(ignoreParam);
                        }
                        else
                        {
                            String maxColLen = line.substring(0, line.indexOf(' '));
                            line = line.substring(maxColLen.length() + 1);

                            String isVis = line.substring(0, line.indexOf(' '));
                            line = line.substring(isVis.length() + 1);

                            int nextSpace = line.indexOf(' ');
                            String parserType = line;
                            String[] params = null;
                            if ( nextSpace >= 0 )
                            {
                                parserType = line.substring(0, nextSpace);
                                line = line.substring(parserType.length());
                                params = line.trim().split(" ");
                            }

                            entries.add(new LogfileColumnConfig(columnName,
                                                                parserType,
                                                                Integer.valueOf(maxColLen),
                                                                "false".equals(isVis),
                                                                params));
                        }
                        line = in.readLine();
                    }
                    configs.put(filePattern, new LogfileRowConfig(fileName, filePattern, toIgnore,

                                                                  entries.toArray(new LogfileColumnConfig[entries.size()])));
                }
                else
                {
                    line = in.readLine();
                }
            }

        }
        catch ( IOException e )
        {
            LogViewer.handleException(e);
        }

    }

    private boolean isNumeric(String ignoreParam)
    {
        try
        {
            Integer.valueOf(ignoreParam);
            return true;
        }
        catch ( NumberFormatException nfe )
        {
            return false;
        }
    }

    public LogfileRowConfig findMatchingConfig(Logfile aLogfile)
    {
        return configs.get(aLogfile.getDisplayName());
    }

    public LogfileColumn[] parseLine(LogfileRowConfig aConfig, String aLine)
    {
        String line = aLine;
        if ( aConfig == null )
        {
            return new LogfileColumn[] { new LogfileColumn("LINE", line) };
        }

        List<LogfileColumn> columns = new ArrayList<>();

        boolean ignoreLine = false;
        for ( String toIgnore : aConfig.getEffectiveIgnoreList() )
        {
            if ( line.startsWith(toIgnore) )
            {
                for ( int i = 0; i < aConfig.getEntries().length; i++ )
                {
                    columns.add(new LogfileColumn(aConfig.getEntries()[i].getColumnName(),
                                                  i == aConfig.getEntries().length - 1 ? aLine : ""));
                }
                ignoreLine = true;
            }
        }

        if ( !ignoreLine )
        {
            for ( LogfileColumnConfig entry : aConfig.getEntries() )
            {
                String parsedLine = entry.getParser().parse(line, entry);
                if ( parsedLine.length() < line.length() )
                {
                    line = line.substring(parsedLine.length(), line.length());
                }
                columns.add(new LogfileColumn(entry.getColumnName(), parsedLine.trim()));
            }
        }
        return columns.toArray(new LogfileColumn[columns.size()]);
    }

    @Override
    public void displayEditPanel()
    {
        editPanel = new LogfileParserEditPanel();
        new ConfigDialog(this, editPanel).setVisible(true);
    }

    @Override
    public void updateConfig()
    {
        configs.clear();
        List<LogfileRowConfig> entries = editPanel.getEntries();
        for ( LogfileRowConfig config : entries )
        {
            configs.put(config.getMatcher(), config);
        }

        saveConfig();
    }

    public void saveConfig()
    {
        File f = new File("columnFormatter.config");
        if ( f.exists() )
        {
            f.delete();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(f)))
        {
            for ( LogfileRowConfig row : configs.values() )
            {
                out.println("[" + row.getName() + "=" + row.getMatcher() + "]");
                for ( LogfileColumnConfig col : row.getEntries() )
                {
                    String params = "";
                    if ( col.getParams() != null && col.getParams().length > 0 )
                    {
                        params = String.join(" ", col.getParams());
                    }

                    out.println(
                            "\"" + col.getColumnName() + "\" " + col.getMaxColumnSize() + " " + (col.isInitiallyHidden()
                                    ? "false"
                                    : "true") + " " + col.getEntryType() + (!params.isEmpty() ? " " + params : ""));
                }
                for ( String ignore : row.getIgnoreList() )
                {
                    out.println("IGNORE " + ignore);
                }
            }
        }
        catch ( IOException e )
        {
            LogViewer.handleException(e);
        }
    }

}
