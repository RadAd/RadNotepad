package au.radsoft;

import android.net.Uri;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import java.io.File;
import java.io.InputStream;

import org.mozilla.universalchardet.UniversalDetector;

class Utils
{
    private Utils() { }
    
    public static String getFileExtension(Uri uri)
    {
        if (uri == null)
            return null;
        android.webkit.MimeTypeMap mtm = android.webkit.MimeTypeMap.getSingleton();
        return mtm.getFileExtensionFromUrl(uri.toString());
    }
    
    public static String getMimeType(Uri uri)
    {
        if (uri == null)
            return null;
        android.webkit.MimeTypeMap mtm = android.webkit.MimeTypeMap.getSingleton();
        String extension = mtm.getFileExtensionFromUrl(uri.toString());
        return mtm.getMimeTypeFromExtension(extension);
    }
    
    public static String detectEncoding(android.content.ContentResolver cr, Uri uri)
        throws java.io.FileNotFoundException, java.io.IOException
    {
        try (InputStream is = cr.openInputStream(uri))
        {
            UniversalDetector detector = new UniversalDetector(null);
            byte[] buf = new byte[4096];
            int nread;
            while ((nread = is.read(buf)) > 0 && !detector.isDone())
            {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();

            return detector.getDetectedCharset();
            //detector.reset();
        }
    }
    
    public static <T> T ifNull(T input, T ifnull)
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
    
    public static String getUserFriendlyName(Throwable t)
    {
        if (t instanceof OutOfMemoryError)
            return "Out of memory";
        else if (t instanceof java.io.FileNotFoundException)
            return "File not found";
        else if (t instanceof java.io.IOException)
            return "IO error";
        else
            return t.getClass().getSimpleName();
    }
    
    public static String getMessage(Throwable t)
    {
        String msg = t.getLocalizedMessage();
        if (msg == null || msg.length() == 0)
            return getUserFriendlyName(t);
        else
            return getUserFriendlyName(t) + ": " + msg;
    }
    
    public static void enable(MenuItem mi, boolean enable)
    {
        if (mi != null)
        {
            mi.setEnabled(enable);
            android.graphics.drawable.Drawable icon = mi.getIcon();
            if (icon != null)
                icon.setAlpha(enable ? 255 : 130);
        }
    }
    
    public static void check(MenuItem mi, boolean enable)
    {
        if (mi != null)
        {
            mi.setChecked(enable);
        }
    }
    
    public static void check(SubMenu sm, String name)
    {
        if (sm != null)
        {
            for (int i = 0; i < sm.size(); ++i)
            {
                MenuItem mi = sm.getItem(i);
                if (mi.getTitle().equals(name))
                    mi.setChecked(true);
            }
        }
    }
    
    public static void dispatchCharEvents(View v, char[] chars)
    {
        KeyCharacterMap kmap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        KeyEvent es[] = kmap.getEvents(chars);
        for (KeyEvent e : es)
            v.dispatchKeyEvent(e);
    }
}
