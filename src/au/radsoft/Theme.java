package au.radsoft;

import radsoft.syntaxhighlighter.brush.Brush;

import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public class Theme
{
    // From R.array.pref_font_theme_values
    static final String THEME_DEFAULT      = "default";  // R.string.pref_font_theme_default
    static final String THEME_DJANGO       = "django";
    static final String THEME_ECLIPSE      = "eclipse";
    static final String THEME_EMACS        = "emacs";
    static final String THEME_FADE_TO_GREY = "fade_to_grey";
    static final String THEME_MD_ULTRA     = "md_ultra";
    static final String THEME_MIDNIGHT     = "midnight";
    static final String THEME_RDARK        = "rdark";
    
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
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x336442).setStyle(Typeface.ITALIC));
            theme.put(Brush.STRING,         new Style().setFgColor(0x9df39f));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0x96dd3b).setStyle(Typeface.BOLD));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x91bb9e));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xffaa3e));
            theme.put(Brush.VALUE,          new Style().setFgColor(0xf7e741));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xffaa3e));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0xe0e8ff));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0xeb939a));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0x91bb9e));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xedef7d));
            break;
         
        case THEME_ECLIPSE:
            // (C) Code-House
            // http//blog.code-house.org/2009/10/xml-i-adnotacje-kod-ogolnego-przeznaczenia-i-jpa/
            theme = new Theme(THEME_ECLIPSE, 0x000000, 0xffffff, 0xc3defe);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x3f5fbf));
            theme.put(Brush.STRING,         new Style().setFgColor(0x2a00ff));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0x7f0055).setStyle(Typeface.BOLD));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x646464));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xaa7700));
            theme.put(Brush.VALUE,          new Style().setFgColor(0x009900));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xff1493));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0x0066cc));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0x646464));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0xff1493));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xff0000));
            break;
         
        case THEME_EMACS:
            // Emacs SyntaxHighlighter theme based on theme by Joshua Emmons
            // http://www.skia.net/
            theme = new Theme(THEME_EMACS, 0xd3d3d3, 0x000000, 0x2A3133);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0xff7d27));
            theme.put(Brush.STRING,         new Style().setFgColor(0xff9e7b));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0x00ffff));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0xaec4de));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xffaa3e));
            theme.put(Brush.VALUE,          new Style().setFgColor(0x009900));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0x81cef9));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0xff9e7b));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0xebdb8d));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0xff7d27));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xaec4de));
            break;
         
        case THEME_FADE_TO_GREY:
            // Fade to Grey SyntaxHighlighter theme based on theme by Brasten Sager
            // http//www.ibrasten.com/
            theme = new Theme(THEME_FADE_TO_GREY, 0xFFFFFF, 0x121212, 0x2C2C29);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x696854));
            theme.put(Brush.STRING,         new Style().setFgColor(0xe3e658));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0xd01d33));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x435a5f));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0x898989));
            theme.put(Brush.VALUE,          new Style().setFgColor(0x009900));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xaaaaaa).setStyle(Typeface.BOLD));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0x96daff));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0xffc074));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0x4a8cdb));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0x96daff));
            break;
         
        case THEME_MD_ULTRA:
            // MDUltra SyntaxHighlighter theme based on Midnight Theme
            // http://www.mddev.co.uk/
            theme = new Theme(THEME_MD_ULTRA, 0x00ff00, 0x222222, 0x253e5a);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x428bdd));
            theme.put(Brush.STRING,         new Style().setFgColor(0x00ff00));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0xaaaaff));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x8aa6c1));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0x00ffff));
            theme.put(Brush.VALUE,          new Style().setFgColor(0xf7e741));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xff8000));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0xffff00));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0xff0000));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0xffff00));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xffaa3e));
            break;
         
        case THEME_MIDNIGHT:
            // Midnight SyntaxHighlighter theme based on theme by J.D. Myers
            // http://webdesign.lsnjd.com/
            theme = new Theme(THEME_MIDNIGHT, 0xd1edff, 0x0f192a, 0x253e5a);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x428bdd));
            theme.put(Brush.STRING,         new Style().setFgColor(0x1dc116));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0xb43d3d));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x8aa6c1));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xffaa3e));
            theme.put(Brush.VALUE,          new Style().setFgColor(0xf7e741));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xffaa3e));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0xe0e8ff));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0xf8bb00));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0xffffff));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xffaa3e));
            break;
         
        case THEME_RDARK:
            // RDark SyntaxHighlighter theme based on theme by Radu Dineiu
            // http://www.vim.org/scripts/script.php?script_id=1732
            theme = new Theme(THEME_RDARK, 0xb9bdb6, 0x1b2426, 0x323E41);
            theme.put(Brush.COMMENTS,       new Style().setFgColor(0x878a85));
            theme.put(Brush.STRING,         new Style().setFgColor(0x5ce638));
            theme.put(Brush.KEYWORD,        new Style().setFgColor(0x5ba1cf));
            theme.put(Brush.PREPROCESSOR,   new Style().setFgColor(0x435a5f));
            theme.put(Brush.VARIABLE,       new Style().setFgColor(0xffaa3e));
            theme.put(Brush.VALUE,          new Style().setFgColor(0x009900));
            theme.put(Brush.FUNCTIONS,      new Style().setFgColor(0xffaa3e));
            theme.put(Brush.CONSTANTS,      new Style().setFgColor(0xe0e8ff));
            theme.put(Brush.COLOR1,         new Style().setFgColor(0xe0e8ff));
            theme.put(Brush.COLOR2,         new Style().setFgColor(0xffffff));
            theme.put(Brush.COLOR3,         new Style().setFgColor(0xffaa3e));
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
                StyleSpan.class,
            };
        return types;
    }
}
