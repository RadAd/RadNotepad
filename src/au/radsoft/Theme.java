package au.radsoft;

import radsoft.syntaxhighlighter.brush.Brush;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class Theme
{
    static Theme getThemeByName(String name)
    {
        Theme theme = new Theme(0xFF000000);
        theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0xFFFF6820));
        theme.put(Brush.KEYWORD,        new Style().setFgColor(0xFF00FFFF));
        theme.put(Brush.STRING,         new Style().setFgColor(0xFFFF00FF));
        theme.put(Brush.VALUE,          new Style().setFgColor(0xFFFF00FF));
        theme.put(Brush.VARIABLE,       new Style().setFgColor(0xFF007F7F));
        theme.put(Brush.COMMENTS,       new Style().setFgColor(0xFF00FF00));
        theme.put(Brush.COLOR1,         new Style().setFgColor(0xFFFFFF00));
        return theme;
    }
    
    public static class Style
    {
    	int fgcolor = -1;
    	int bgcolor = -1;
        
        Style setFgColor(int c)
        {
            fgcolor = c;
            return this;
        }
        
        Style setBgColor(int c)
        {
            bgcolor = c;
            return this;
        }
    }
    
    private int bgcolor;
    private java.util.Map<String, Style> styles = new java.util.HashMap<String, Style>();
    
    Theme(int c)
    {
        bgcolor = c;
    }
    
    void put(String n, Style s)
    {
        styles.put(n, s);
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
    
    static Class<Object>[] getSpanTypes()
    {
        Class<Object>[] types =
            {
                (Class<Object>) ForegroundColorSpan.class,
                (Class<Object>) BackgroundColorSpan.class,
            };
        return types;
    }
}
