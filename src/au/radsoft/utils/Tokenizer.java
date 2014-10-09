package au.radsoft.utils;

public class Tokenizer
{
    private final CharSequence s;
    private final java.util.Comparator<CharSequence> comp; 
    private int i = 0;
    private int j = 0;
    
    public String mTokenStart = "_";   // An array of individual chars
    public String mTokenChars = "_";   // An array of individual chars
    public String mLineComment = "//";
    public String[] mSpecials = { "\"", "'", "/*", "*/" };
    public char mEscape = '\\';
    
    public enum Type { WHITE_SPACE, TOKEN, NUMBER, LINE_COMMENT, SPECIAL, UNKNOWN, END };
    
    public Tokenizer(CharSequence s, java.util.Comparator<CharSequence> comp)
    {
        this.s = s;
        this.comp = comp;
        if (mSpecials != null)
            java.util.Arrays.sort(mSpecials);
    }
    
    public Tokenizer(CharSequence s, boolean caseSensitive)
    {
        this(s, caseSensitive ? new CharSequenceUtils.Comparator() : new CharSequenceUtils.ComparatorIgnoreCase());
    }
    
    public Type getNextToken(boolean skipws)
    {
        i = j;
        
        char c = charAt(j);
        while (Character.isWhitespace(c))
        {
            c = charAt(++j);
        };
        
        if (!skipws && i != j)
        {
            return Type.WHITE_SPACE;
        }
        
        i = j;
        
        String op = null;
        if (i >= s.length())
        {
            j = i + 1;
            return Type.END;
        }
        else if(mLineComment != null && is(i, mLineComment))
        {
            j = i;
            do
            {
                c = charAt(++j);
            } while (c != '\n' && c != 0);
            return Type.LINE_COMMENT;
        }
        else if ((op = findSpecial(i)) != null)
        {
            j = i + op.length();
            return Type.SPECIAL;
        }
        else if (in(mTokenStart, c) || Character.isAlphabetic(c))
        {
            j = i;
            do
            {
                c = charAt(++j);
            } while (in(mTokenChars, c) || Character.isAlphabetic(c) || Character.isDigit(c));
            return Type.TOKEN;
        }
        else if (is(i, "0x"))
        {
            j = i + 1;
            do
            {
                c = charAt(++j);
            } while (Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
            return Type.NUMBER;
        }
        else if (Character.isDigit(c))
        {
            j = i;
            do
            {
                c = charAt(++j);
            } while (Character.isDigit(c));
            if (c == '.')
            {
                ++j;
                do
                {
                    c = charAt(++j);
                } while (Character.isDigit(c));
            }
            return Type.NUMBER;
        }
        else
        {
            if (c == mEscape)
                ++i;
            j = i + 1;
            return Type.UNKNOWN;
        }
    }
    
    public CharSequence get()
    {
        if (i >= s.length())
            return "";
        else
            return s.subSequence(i, j);
    }
    
    public int getStart()
    {
        return i;
    }
    
    public int getEnd()
    {
        return j;
    }
    
    private boolean in(String s, char c)
    {
        return s != null && s.indexOf(c) >= 0;
    }
    
    private char charAt(int i)
    {
        if (i >= s.length())
            return 0;
        else
            return s.charAt(i);
    }
    
    private String findSpecial(int b)
    {
        // TODO
        // This could be faster
        // Either use binary search or maybe a state machine
        if (mSpecials != null)
        {
            for (String s : mSpecials)
            {
                if (is(b, s))
                    return s;
            }
        }
        return null;
    }
    
    private boolean is(int b, String f)
    {
        int e = Math.min(b + f.length(), s.length());
        return comp.compare(f, s.subSequence(b, e)) == 0;
    }
}
