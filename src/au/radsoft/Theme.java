package au.radsoft;

import radsoft.syntaxhighlighter.brush.Brush;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class Theme
{
    static Theme getThemeByName(String name)
    {
        Theme theme = null;
        switch (name)
        {
        default:
            theme = new Theme(0x000000, 0xFFFFFF);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x008200));
            theme.put(Brush.STRING,         new Style().setFgColor(0x0000ff));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0x006699));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x646464));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xaa7700));
            theme.put(Brush.VALUE,          new Style().setFgColor(0x009900));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xff1493));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0x0066cc));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0x646464));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0xff1493));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xff0000));
            break;
         
        case "Django":
            theme = new Theme(0xf8f8f8, 0x0a2b1d);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x336442));
            theme.put(Brush.STRING,         new Style().setFgColor(0x9df39f));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0x96dd3b));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x91bb9e));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xffaa3e));
            theme.put(Brush.VALUE,          new Style().setFgColor(0xf7e741));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xffaa3e));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0xe0e8ff));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0xeb939a));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0x91bb9e));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xedef7d));
            break;
        }
        return theme;
    }
    
    public static class Style
    {
    	int fgcolor = -1;
    	int bgcolor = -1;
        
        Style setFgColor(int c)
        {
            fgcolor = 0xFF000000 | c;
            return this;
        }
        
        Style setBgColor(int c)
        {
            bgcolor = 0xFF000000 | c;
            return this;
        }
    }
    
    private int fgcolor;
    private int bgcolor;
    private java.util.Map<String, Style> styles = new java.util.HashMap<String, Style>();
    
    Theme(int cfg, int cbg)
    {
        fgcolor = 0xFF000000 | cfg;
        bgcolor = 0xFF000000 | cbg;
    }
    
    void put(String n, Style s)
    {
        styles.put(n, s);
    }
    
    int getFgColor()
    {
        return fgcolor;
    }
    
    int getBgColor()
    {
        return bgcolor;
    }
    
    Object[] getSpans(String n)
    {
        java.util.List<Object> spans = new java.util.ArrayList<Object>();
        
        Style s = styles.get(n);
        if (s != null && s.fgcolor != -1)
            spans.add(new ForegroundColorSpan(s.fgcolor));
        if (s != null && s.bgcolor != -1)
            spans.add(new BackgroundColorSpan(s.bgcolor));
        
        Object[] aspans = new Object[spans.size()];
        spans.toArray(aspans);
        return aspans;
        
    }
    
    static Class<? extends Object>[] getSpanTypes()
    {
        Class<? extends Object>[] types = new Class[]
            {
                ForegroundColorSpan.class,
                BackgroundColorSpan.class,
            };
        return types;
    }
}
