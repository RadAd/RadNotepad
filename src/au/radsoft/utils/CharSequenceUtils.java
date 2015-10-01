package au.radsoft.utils;

public class CharSequenceUtils
{
    private CharSequenceUtils() { }
    
    public static int indexOf(CharSequence cs, char c)
    {
        return indexOf(cs, c, 0);
    }
    
    public static int indexOf(CharSequence cs, char c, int start)
    {
        int o = start;
        while (o < cs.length())
        {
            if (cs.charAt(o) == c)
                return o;
            ++o;
        }
        return -1;
    }
    
    public static int indexOfIgnoreCase(CharSequence cs, char c)
    {
        return indexOfIgnoreCase(cs, c, 0);
    }
    
    public static int indexOfIgnoreCase(CharSequence cs, char c, int start)
    {
        int o = start;
        c = Character.toUpperCase(c);
        while (o < cs.length())
        {
            if (Character.toUpperCase(cs.charAt(o)) == c)
                return o;
            ++o;
        }
        return -1;
    }
    
    public static int indexOf(CharSequence cs, CharSequence search)
    {
        return indexOf(cs, search, 0);
    }
    
    public static int indexOf(CharSequence cs, CharSequence search, int start)
    {
        if (search.length() == 0)
            return -1;
        int o = start;
        while ((o = indexOf(cs, search.charAt(0), o)) != -1)
        {
            if ((o + search.length()) > cs.length())
                return -1;
            CharSequence sub = cs.subSequence(o, o + search.length());
            if (compare(search, sub) == 0)
                return o;
            ++o;
        }
        return -1;
    }
    
    public static int indexOfIgnoreCase(CharSequence cs, CharSequence search)
    {
        return indexOfIgnoreCase(cs, search, 0);
    }
    
    public static int indexOfIgnoreCase(CharSequence cs, CharSequence search, int start)
    {
        if (search.length() == 0)
            return -1;
        int o = start;
        while ((o = indexOfIgnoreCase(cs, search.charAt(0), o)) != -1)
        {
            if ((o + search.length()) > cs.length())
                return -1;
            CharSequence sub = cs.subSequence(o, o + search.length());
            if (compareIgnoreCase(search, sub) == 0)
                return o;
            ++o;
        }
        return -1;
    }
    
    public static int lastIndexOf(CharSequence cs, char c)
    {
        return lastIndexOf(cs, c, cs.length() - 1);
    }
    
    public static int lastIndexOf(CharSequence cs, char c, int last)
    {
        int o = last;
        while (o >= 0)
        {
            if (cs.charAt(o) == c)
                return o;
            --o;
        }
        return -1;
    }
    
    public static int lastIndexOfIgnoreCase(CharSequence cs, char c)
    {
        return lastIndexOf(cs, c, cs.length() - 1);
    }
    
    public static int lastIndexOfIgnoreCase(CharSequence cs, char c, int last)
    {
        c = Character.toUpperCase(c);
        int o = last;
        while (o >= 0)
        {
            if (Character.toUpperCase(cs.charAt(o)) == c)
                return o;
            --o;
        }
        return -1;
    }
    
    public static int lastIndexOf(CharSequence cs, CharSequence search)
    {
        return lastIndexOf(cs, search, cs.length() - search.length());
    }
    
    public static int lastIndexOf(CharSequence cs, CharSequence search, int last)
    {
        if (search.length() == 0)
            return -1;
        int o = last;
        if (o > (cs.length() - search.length()))
            o = cs.length() - search.length();
        while ((o = lastIndexOf(cs, search.charAt(0), o)) != -1)
        {
            CharSequence sub = cs.subSequence(o, o + search.length());
            if (compare(search, sub) == 0)
                return o;
            --o;
        }
        return -1;
    }
    
    public static int lastIndexOfIgnoreCase(CharSequence cs, CharSequence search)
    {
        return lastIndexOfIgnoreCase(cs, search, cs.length() - search.length());
    }
    
    public static int lastIndexOfIgnoreCase(CharSequence cs, CharSequence search, int last)
    {
        if (search.length() == 0)
            return -1;
        int o = last;
        if (o > (cs.length() - search.length()))
            o = cs.length() - search.length();
        while ((o = lastIndexOf(cs, search.charAt(0), o)) != -1)
        {
            CharSequence sub = cs.subSequence(o, o + search.length());
            if (compareIgnoreCase(search, sub) == 0)
                return o;
            --o;
        }
        return -1;
    }
    
    public static int compare(CharSequence l, CharSequence r)
    {
        // Note: compares numerically, ignores locale
        int d = 0;
        if (l == r)
        {
            d = 0;
        }
        else if (l == null)
        {
            d = -1;
        }
        else if (r == null)
        {
            d = 1;
        }
        else
        {
            for (int i = 0; d == 0; ++i)
            {
                if (i == l.length() || i == r.length())
                {
                    d = l.length() - r.length();
                    break;
                }
                else
                    d = l.charAt(i) - r.charAt(i);
            }
        }
        return d;
    }
    
    public static int compareIgnoreCase(CharSequence l, CharSequence r)
    {
        // Note: compares numerically, ignores locale
        int d = 0;
        if (l == r)
        {
            d = 0;
        }
        else if (l == null)
        {
            d = -1;
        }
        else if (r == null)
        {
            d = 1;
        }
        else
        {
            for (int i = 0; d == 0; ++i)
            {
                if (i == l.length() || i == r.length())
                {
                    d = l.length() - r.length();
                    break;
                }
                else
                    d = Character.toUpperCase(l.charAt(i)) - Character.toUpperCase(r.charAt(i));
            }
        }
        return d;
    }
    
    public static class Comparator implements java.util.Comparator<CharSequence>
    {
        @Override
        public int compare(CharSequence o1, CharSequence o2)
        {
            return CharSequenceUtils.compare(o1, o2);
        }
    }
    
    public static class ComparatorIgnoreCase implements java.util.Comparator<CharSequence>
    {
        @Override
        public int compare(CharSequence o1, CharSequence o2)
        {
            return CharSequenceUtils.compareIgnoreCase(o1, o2);
        }
    }
}
