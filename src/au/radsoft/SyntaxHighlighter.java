package au.radsoft;

import android.text.Editable;
import au.radsoft.utils.CharSequenceUtils;

// TODO

// Case sensitive/insensitive keywords

// Support preprocessor '@' for java and '#' for C/C++ and '<' for xml
//      Java allows for any token after '@'
//      xml allows for any token after '@'
//      C/C++ has defined token after '#' ie "if", "endif", etc

// Batch file labels must begin with ':'
// Batch file variables %...% !...! %d %~...d

// xml distinguish between attributes and text ie inside/outside of </>

// Begin end comments
//      Java, C/C++  /*...*/
//      xml <!--...-->

class SyntaxHighlighter
{
    static class Scheme
    {
        Scheme(String name, String tokenStart, String lineComment, String[] keywords, String[] booleans)
        {
            this.name = name;
            this.tokenStart = tokenStart;
            this.lineComment = lineComment;
            this.keywords = keywords;
            this.booleans = booleans;
        }
        
        final String name;
        final String tokenStart;
        final String lineComment;
        final String[] keywords;
        final String[] booleans;
    }
    
    private static String[] sStandardBooleans = { "true", "false" };
    private static String[] sNull = { };
    
    private static String[] sJavaKeywords =
        { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
          "continue", "default", "do", "double", "else", "extends", "final", "finally", "float", "for",
          "future", "generic", "goto", "if", "implements", "import", "inner", "instanceof", "int", "interface",
          "long", "native", "new", "null", "operator", "outer", "package", "private", "protected",
          "public", "rest", "return", "short", "static", "super", "switch", "synchronized", "this",
          "throw", "throws", "transient", "try", "var", "void", "volatile", "while" };
    private static String[] sBatchKeywords =
        { "assoc", "break", "call", "cd", "chdir", "cls", "color", "copy", "date", "del", "dir",
          "echo", "endlocal", "erase", "exit", "ftype", "goto", "graftabl", "if", "md", "mkdir",
          "mklink", "move", "path", "pause", "popd", "prompt", "pushd", "rd", "rem", "ren", "rename",
          "rmdir", "set", "setlocal", "shift", "start", "time", "title", "type", "ver", "verify", "vol" };
          
    private static Scheme mSchemeJava = new Scheme("Java", "_@", "//", sJavaKeywords, sStandardBooleans);
    private static Scheme mSchemeBatch = new Scheme("Batch", ":%!", "rem ", sBatchKeywords, sNull);
        
    static Scheme getScheme(android.net.Uri uri)
    {
        if (uri == null)
            return null;
        String ext = Utils.getFileExtension(uri);
        switch (ext)
        {
        case "java":
            return mSchemeJava;
            
        case "bat":
        case "cmd":
            return mSchemeBatch;
            
        default:
            return null;
        }
    }
    
    private Editable mEditable;
    private Scheme mScheme;
    
    SyntaxHighlighter(Editable e, Scheme scheme)
    {
        mEditable = e;
        mScheme = scheme;
    }
    
    void highlight()
    {
        Tokenizer t = new Tokenizer(mEditable);
        t.mTokenStart = mScheme.tokenStart;
        t.mLineComment = mScheme.lineComment;
        java.util.Comparator<CharSequence> comp = new CharSequenceUtils.Comparator();
        
        boolean cont = true;
        while (cont)
        {
            Tokenizer.Type tt = t.getNextToken();
            
            Object span = null;
            switch (tt)
            {
            case TOKEN:
                if (t.mTokenStart.indexOf(t.get().charAt(0)) >= 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFF6820);
                else if (java.util.Arrays.binarySearch(mScheme.keywords, t.get(), comp) >= 0)
                //else if (java.util.Arrays.binarySearch(mScheme.keywords, t.get().toString()) >= 0)
                    span = new android.text.style.ForegroundColorSpan(0xFF00FFFF);
                else if (java.util.Arrays.binarySearch(mScheme.booleans, t.get(), comp) >= 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFF00FF);
                break;
                
            case NUMBER:
                span = new android.text.style.ForegroundColorSpan(0xFFFF00FF);
                break;
                
            case STRING:
                span = new android.text.style.ForegroundColorSpan(0xFFFF00FF);
                break;
                
            case LINE_COMMENT:
                span = new android.text.style.ForegroundColorSpan(0xFF00FF00);
                break;
                
            case END:
                cont = false;
                break;
            }
            if (span != null)
                mEditable.setSpan(span, t.getStart(), t.getEnd(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
