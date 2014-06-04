package au.radsoft;

public class Tokenizer
{
    private final CharSequence s;
    private int i = 0;
    private int j = 0;
    
    String mTokenStart = "_";   // An array of individual chars
    String mLineComment = "//";
    String[] mSpecials = { "\"", "'", "/*", "*/" };
    
    public enum Type { WHITE_SPACE, TOKEN, NUMBER, LINE_COMMENT, SPECIAL, UNKNOWN, END };
    
    public Tokenizer(CharSequence s)
    {
        this.s = s;
        if (mSpecials != null)
            java.util.Arrays.sort(mSpecials);
    }
    
    public Type getNextToken(boolean skipws)
    {
        i = j - 1;
        char c = 0;
        do
        {
            c = charAt(++i);
        } while (Character.isWhitespace(c));
        
        if (!skipws && i != j)
        {
            j = i;
            return Type.WHITE_SPACE;
        }
        
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
        else if ((mTokenStart != null && mTokenStart.indexOf(c) >= 0) || Character.isAlphabetic(c))
        {
            j = i;
            do
            {
                c = charAt(++j);
            } while (c == '_' || Character.isAlphabetic(c) || Character.isDigit(c));
            return Type.TOKEN;
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
        else if (is(i, "0x"))
        {
            j = i + 1;
            do
            {
                c = charAt(++j);
            } while (Character.isDigit(c) || (c > 'a' && c <= 'f') || (c > 'A' && c <= 'F'));
            return Type.NUMBER;
        }
        else if ((op = findSpecial(i)) != null)
        {
            j = i + op.length();
            return Type.SPECIAL;
        }
        else
        {
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
        for (int i = 0; i < f.length(); ++i)
        {
            if (f.charAt(i) != charAt(b + i))
                return false;
        }
        return true;
    }
}
