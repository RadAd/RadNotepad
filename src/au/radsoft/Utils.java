package au.radsoft;

import android.net.Uri;

import java.io.File;

class Utils
{
    private Utils() { }
    
    public static String getMimeType(Uri uri)
    {
        android.webkit.MimeTypeMap mtm = android.webkit.MimeTypeMap.getSingleton();
        String extension = mtm.getFileExtensionFromUrl(uri.toString());
        return mtm.getMimeTypeFromExtension(extension);
    }
    
    public static String ifNull(String input, String ifnull)
    {
        return input == null ? ifnull : input;
    }
    
    public static long getLastModified(Uri uri)
    {
        if (uri != null && uri.getScheme().equals("file"))
        {
            File f = new File(uri.getPath());
            return f.lastModified();
        }
        else
            return -1;
    }
    
    public static int find(CharSequence cs, int o, char c)
    {
        while (o < cs.length())
        {
            if (cs.charAt(o) == c)
                return o;
            ++o;
        }
        return -1;
    }
    
    static int find(CharSequence cs, int o, String search)
    {
        if (search.length() == 0)
            return -1;
        while ((o = find(cs, o, search.charAt(0))) != -1)
        {
            if ((o + search.length()) > cs.length())
                return -1;
            CharSequence sub = cs.subSequence(o, o + search.length());
            if (search.contentEquals(sub))
                return o;
            ++o;
        }
        return -1;
    }
}
