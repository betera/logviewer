package com.betera.logviewer;

public class Util
{

    public static boolean containsOnlySpaces(String aText)
    {
        for ( int i = 0; i < aText.length(); i++ )
        {
            if ( aText.charAt(i) != ' ' )
            {
                return false;
            }
        }
        return true;
    }

    public static StringReadResult readQuotationString(String aLine)
    {
        int i = 0;
        String ret = "";
        boolean inString = false;
        while ( i < aLine.length() )
        {
            char c = aLine.charAt(i);
            if ( c == '\"' )
            {
                if ( inString )
                {
                    return new StringReadResult(ret, true, ret.length() + 2);
                }
                inString = true;
            }
            else if ( inString )
            {
                ret += aLine.charAt(i);
            }
            i++;
        }
        return null;
    }

    public static StringReadResult readString(String aLine)
    {
        if ( aLine.startsWith("\"") )
        {
            return readQuotationString(aLine);
        }
        else if ( aLine.indexOf(" ") > 0 )
        {
            String value = aLine.substring(0, aLine.indexOf(" "));
            return new StringReadResult(value, false, value.length());
        }
        else
        {
            return new StringReadResult(aLine, false, aLine.length());
        }
    }

    public static class StringReadResult
    {
        private boolean isQuotationString = false;
        private int readLength = 0;
        private String value;

        StringReadResult(String value, boolean isQuotationString, int readLength)
        {
            this.value = value;
            this.isQuotationString = isQuotationString;
            this.readLength = readLength;
        }

        public String getValue()
        {
            return value;
        }

        public boolean isQuotationString()
        {
            return isQuotationString;
        }

        public int getReadLength()
        {
            return readLength;
        }
    }

}
