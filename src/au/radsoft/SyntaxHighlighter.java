package au.radsoft;

import android.text.Editable;
import au.radsoft.utils.CharSequenceUtils;

// TODO

// Case sensitive/insensitive keywords

// xml distinguish between attributes and text ie inside/outside of </>

class SyntaxHighlighter
{
    static class Scheme
    {
        Scheme(String name, String tokenStart, String preProcessor, String lineComment, String[] keywords, String[] literals, String[] specials)
        {
            this.name = name;
            this.tokenStart = tokenStart;
            this.preProcessor = preProcessor;
            this.lineComment = lineComment;
            this.keywords = keywords;
            this.literals = literals;
            this.specials = specials;
        }
        
        final String name;
        final String tokenStart;
        final String preProcessor;
        final String lineComment;
        final String[] keywords;
        final String[] literals;
        final String[] specials;
    }
    
    private static String[] sJavaLiterals = { "true", "false", "null" };
    private static String[] sBatchLiterals = { "CON", "AUX", "PRN", "NUL" };
    
    private static String[] sJavaSpecials = { "\"", "'", "/*", "*/" };
    private static String[] sBatchSpecials = { "%", "!" };
    private static String[] sConfSpecials = { "[", "]" };
    private static String[] sXmlSpecials = { "\"", "<!--", "-->" };
    
    private static String[] sJavaKeywords =
        { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
          "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
          "goto", "if", "implements", "import", "instanceof", "int", "interface",
          "long", "native", "new", "package", "private", "protected",
          "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
          "throw", "throws", "transient", "try", "void", "volatile", "while" };
    private static String[] sBatchKeywords =
        { "assoc", "break", "call", "cd", "chdir", "cls", "color", "copy", "date", "del", "dir",
          "echo", "endlocal", "erase", "exit", "ftype", "goto", "graftabl", "if", "md", "mkdir",
          "mklink", "move", "path", "pause", "popd", "prompt", "pushd", "rd", "rem", "ren", "rename",
          "rmdir", "set", "setlocal", "shift", "start", "time", "title", "type", "ver", "verify", "vol" };
          
    private static Scheme mSchemeJava  = new Scheme("Java",  "_", "@", "//",   sJavaKeywords,  sJavaLiterals,  sJavaSpecials);
    private static Scheme mSchemeBatch = new Scheme("Batch", ":", "",  "rem ", sBatchKeywords, sBatchLiterals, sBatchSpecials);
    private static Scheme mSchemeConf  = new Scheme("Conf",  "",  "",  "#",    null,           null,           sConfSpecials);
    private static Scheme mSchemeXml   = new Scheme("Xml",   "",  "",  null,   null,           null,           sXmlSpecials);
        
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
            
        case "conf":
        case "ini":
            return mSchemeConf;
            
        case "xml":
            return mSchemeXml;
            
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
    
    static void remove(Editable editable, int start, int end)
    {
        android.text.style.ForegroundColorSpan[] spans = editable.getSpans(start, end, android.text.style.ForegroundColorSpan.class);
        for (Object s : spans)
            editable.removeSpan(s);
    }
    
    void highlight(int start, int end)
    {
        remove(mEditable, start, end);
        
        Tokenizer t = new Tokenizer(mEditable.subSequence(start, end));
        t.mTokenStart = mScheme.tokenStart;
        t.mLineComment = mScheme.lineComment;
        t.mSpecials = mScheme.specials;
        if (t.mSpecials != null)
            java.util.Arrays.sort(t.mSpecials);
        java.util.Comparator<CharSequence> comp = new CharSequenceUtils.Comparator();
        CharSequence lastToken = null;
        
        boolean cont = true;
        while (cont)
        {
            Tokenizer.Type tt = t.getNextToken();
            
            Object span = null;
            int spanstart = t.getStart() + start;
            switch (tt)
            {
            case UNKNOWN:
                if (CharSequenceUtils.compare(t.get(), mScheme.preProcessor) == 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFF6820);
                    
            case TOKEN:
                if (t.mTokenStart.indexOf(t.get().charAt(0)) >= 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFFFF00);
                else if (CharSequenceUtils.compare(lastToken, mScheme.preProcessor) == 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFF6820);
                else if (mScheme.keywords != null && java.util.Arrays.binarySearch(mScheme.keywords, t.get(), comp) >= 0)
                    span = new android.text.style.ForegroundColorSpan(0xFF00FFFF);
                else if (mScheme.literals != null && java.util.Arrays.binarySearch(mScheme.literals, t.get(), comp) >= 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFF00FF);
                break;
                
            case NUMBER:
                span = new android.text.style.ForegroundColorSpan(0xFFFF00FF);
                break;
                
            case LINE_COMMENT:
                span = new android.text.style.ForegroundColorSpan(0xFF00FF00);
                break;
                
            case SPECIAL:
                {
                    CharSequence op = t.get();
                    int color = 0xFFFF00FF;
                    if (CharSequenceUtils.compare(op, "/*") == 0)
                    {
                        op = "*/";
                        color = 0xFF00FF00;
                    }
                    else if (CharSequenceUtils.compare(op, "<!--") == 0)
                    {
                        op = "-->";
                        color = 0xFF00FF00;
                    }
                    else if (CharSequenceUtils.compare(op, "[") == 0)
                    {
                        op = "]";
                        color = 0xFFFFFF00;
                    }
                    findSpecial(t, op);
                    span = new android.text.style.ForegroundColorSpan(color);
                }
                break;
                
            case END:
                cont = false;
                break;
            }
            if (span != null)
                mEditable.setSpan(span, spanstart, t.getEnd() + start, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            lastToken = t.get();
        }
    }
    
    private void findSpecial(Tokenizer t, CharSequence end)
    {
        boolean cont = true;
        while (cont)
        {
            Tokenizer.Type tt = t.getNextToken();
            switch (tt)
            {
            case SPECIAL:
                cont = CharSequenceUtils.compare(t.get(), end) != 0;
                break;
                
            case END:
                cont = false;
                break;
            }
        }
    }
}
