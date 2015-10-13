package au.radsoft;

import radsoft.syntaxhighlighter.brush.Brush;

import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public class Theme
{
    static final String THEME_DEFAULT = "Default";
    static final String THEME_DJANGO = "Django";
    
    static Theme getThemeByName(String name)
    {
        Theme theme = null;
        switch (name)
        {
        default:
        case THEME_DEFAULT:
            theme = new Theme(THEME_DEFAULT, 0x000000, 0xFFFFFF, 0xe0e0e0);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x008200));
            theme.put(Brush.STRING,         new Style().setFgColor(0x0000ff));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0x006699).setStyle(Typeface.BOLD));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x646464));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xaa7700));
            theme.put(Brush.VALUE,          new Style().setFgColor(0x009900));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xff1493));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0x0066cc));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0x646464));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0xff1493));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xff0000));
            break;
         
        case THEME_DJANGO:
            theme = new Theme(THEME_DJANGO, 0xf8f8f8, 0x0a2b1d, 0x553a46); //0x233729);
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
        int style = Typeface.NORMAL;
        
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
        
        Style setStyle(int s)
        {
            style = s;
            return this;
        }
    }
    
    private String name;
    private int fgcolor;
    private int bgcolor;
    private int hlcolor;
    private java.util.Map<String, Style> styles = new java.util.HashMap<String, Style>();
    
    Theme(String n, int cfg, int cbg, int chl)
    {
        name = n;
        fgcolor = 0xFF000000 | cfg;
        bgcolor = 0xFF000000 | cbg;
        hlcolor = 0xFF000000 | chl;
    }
    
    void put(String n, Style s)
    {
        styles.put(n, s);
    }
    
    String getName()
    {
        return name;
    }
    
    int getFgColor()
    {
        return fgcolor;
    }
    
    int getBgColor()
    {
        return bgcolor;
    }
    
    int getHlColor()
    {
        return hlcolor;
    }
    
    Object[] getSpans(String n)
    {
        java.util.List<Object> spans = new java.util.ArrayList<Object>();
        
        Style s = styles.get(n);
        if (s != null && s.fgcolor != -1)
            spans.add(new ForegroundColorSpan(s.fgcolor));
        if (s != null && s.bgcolor != -1)
            spans.add(new BackgroundColorSpan(s.bgcolor));
        if (s != null && s.style != Typeface.NORMAL)
            spans.add(new StyleSpan(s.style));
        
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
