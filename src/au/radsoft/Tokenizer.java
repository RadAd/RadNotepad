package au.radsoft;

public class Tokenizer
{
    private final CharSequence s;
    private int i = 0;
    private int j = 0;
    
    String mTokenStart = "_";
    String mLineComment = "//";
    
    public enum Type { TOKEN, NUMBER, STRING, LINE_COMMENT, UNKNOWN, END };
    
    public Tokenizer(CharSequence s)
    {
        this.s = s;
    }
    
    public Type getNextToken()
    {
        i = j - 1;
        char c = 0;
        do
        {
            c = charAt(++i);
        } while (Character.isWhitespace(c));
        
        if (i >= s.length())
        {
            j = i + 1;
            return Type.END;
        }
        else if (c == '\'' || c == '"')
        {
            char b = c;
            j = i;
            do
            {
                c = charAt(++j);
            } while (c != b && c != '\n' && c != 0);
            if (c == b)
                ++j;
            return Type.STRING;
        }
        else if (mTokenStart.indexOf(c) >= 0 || Character.isAlphabetic(c))
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
        else if(is(i, mLineComment))
        {
            j = i;
            do
            {
                c = charAt(++j);
            } while (c != '\n' && c != 0);
            return Type.LINE_COMMENT;
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
