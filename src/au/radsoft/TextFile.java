package au.radsoft;

import android.net.Uri;
import static android.text.TextUtils.indexOf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

class TextFile
{
    static final String LE_WINDOWS = "\r\n";
    static final String LE_UNIX = "\n";
    static final String LE_MAC = "\r";
    
    String mLineEnding = LE_WINDOWS;
    String mFileEncoding = "UTF-8";
    Uri mUri;
    
    CharSequence load(android.content.ContentResolver cr)
        throws java.io.FileNotFoundException, java.io.IOException
    {
        mFileEncoding = Utils.ifNull(Utils.detectEncoding(cr, mUri), "UTF-8");
        InputStream is = cr.openInputStream(mUri);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, mFileEncoding)))
        {
            StringBuffer sb = new StringBuffer();
            
            int c;
            while ((c = br.read()) != -1)
            {
                if (c == '\r')
                {
                    mLineEnding = TextFile.LE_MAC;
                    br.mark(1);
                    int nc = br.read();
                    if (nc != '\n')
                        br.reset();
                    else
                        mLineEnding = TextFile.LE_WINDOWS;
                    c = '\n';
                }
                else if (c == '\n')
                {
                    mLineEnding = TextFile.LE_UNIX;
                }
                sb.append((char) c);
            }

            return sb;
        }
    }
    
    void save(android.content.ContentResolver cr, CharSequence cs)
        throws java.io.FileNotFoundException, java.io.IOException
    {
        OutputStream os = cr.openOutputStream(mUri);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, mFileEncoding)))
        {
            int begin = 0;
            int end = 0;
            while ((end = indexOf(cs, '\n', begin)) != -1)
            {
                CharSequence sub = cs.subSequence(begin, end);
                bw.append(sub);
                bw.write(mLineEnding);
                begin = end + 1;
            }
            CharSequence sub = cs.subSequence(begin, cs.length());
            bw.append(sub);
        }
    }
}
