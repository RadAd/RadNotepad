package au.radsoft;

import android.net.Uri;

import java.io.File;
import java.io.InputStream;

import org.mozilla.universalchardet.UniversalDetector;

class Utils
{
    private Utils() { }
    
    public static String getFileExtension(Uri uri)
    {
        android.webkit.MimeTypeMap mtm = android.webkit.MimeTypeMap.getSingleton();
        return mtm.getFileExtensionFromUrl(uri.toString());
    }
    
    public static String getMimeType(Uri uri)
    {
        android.webkit.MimeTypeMap mtm = android.webkit.MimeTypeMap.getSingleton();
        String extension = mtm.getFileExtensionFromUrl(uri.toString());
        return mtm.getMimeTypeFromExtension(extension);
    }
    
    public static String detectEncoding(android.content.ContentResolver cr, Uri uri)
        throws java.io.FileNotFoundException, java.io.IOException
    {
        InputStream is = cr.openInputStream(uri);

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
    
    public static <T> T[] concatenate(T[] A, T[] B)
    {
        @SuppressWarnings("unchecked")
        T[] C = (T[]) java.lang.reflect.Array.newInstance(A.getClass().getComponentType(), A.length + B.length);
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }
}
