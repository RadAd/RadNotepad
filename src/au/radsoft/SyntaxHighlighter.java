package au.radsoft;

import android.text.Editable;
import au.radsoft.utils.CharSequenceUtils;

// TODO
// xml - distinguish between attributes and text ie inside/outside of </>
// When removing highlight, return range affected to pass on to highlight
// cpp - check preprocessor keywords
// cpp - highlight <filename.h> in preprocessor
// cpp - have category for primitive types ???
// distinguish labels
// batch handle a tab after rem

class SyntaxHighlighter
{
    static class SpecialPair
    {
        SpecialPair(String begin, String end, boolean skipws, int color)
        {
            this.begin = begin;
            this.end = end;
            this.skipws = skipws;
            this.color = color;
        }
        
        final String begin;
        final String end;
        final boolean skipws;
        final int color;
    };
    
    static class Scheme
    {
        Scheme(String name, boolean caseSensitive, String tokenStart, String tokenChars, String preProcessor, String lineComment,
            String[] keywords, String[] literals, SpecialPair[] specialPairs)
        {
            this.name = name;
            this.caseSensitive = caseSensitive;
            this.tokenStart = tokenStart;
            this.tokenChars = tokenChars;
            this.preProcessor = preProcessor;
            this.lineComment = lineComment;
            this.keywords = keywords;
            this.literals = literals;
            this.specialPairs = specialPairs;
        }
        
        final String name;
        final boolean caseSensitive;
        final String tokenStart;
        final String tokenChars;
        final String preProcessor;
        final String lineComment;
        final String[] keywords;
        final String[] literals;
        final SpecialPair[] specialPairs;
        
        String[] getSpecials()
        {
            if (specialPairs == null)
                return null;
            
            int size = 0;
            for (SpecialPair sp : specialPairs)
            {
                size += sp.end != null && sp.end != sp.begin ? 2 : 1;
            }
            
            String[] s = new String[size];
            int i = 0;
            for (SpecialPair sp : specialPairs)
            {
                s[i++] = sp.begin;
                if (sp.end != null && sp.end != sp.begin)
                    s[i++] = sp.end;
            }
            
            return s;
        }
        
        SpecialPair findSpecialPairBegin(CharSequence s, java.util.Comparator<CharSequence> comp)
        {
            if (specialPairs != null)
            {
                for (SpecialPair sp : specialPairs)
                {
                    if (comp.compare(sp.begin, s) == 0)
                        return sp;
                }
            }
            return null;
        }
    }
    
    private static String[] sCppLiterals = { "true", "false", "nullptr" };
    private static String[] sJavaLiterals = { "true", "false", "null" };
    private static String[] sBatchLiterals = { "CON", "AUX", "PRN", "NUL" };
    
    private static SpecialPair[] sCppSpecialPairs = {
        new SpecialPair("\"", "\"", true, 0xFFFF00FF), new SpecialPair("'", "'", true, 0xFFFF00FF), new SpecialPair("/*", "*/", true, 0xFF00FF00) };
    private static SpecialPair[] sBatchSpecialPairs = {
        new SpecialPair("%", "%", false, 0xFF007F7F), new SpecialPair("!", "!", false, 0xFF007F7F),
        new SpecialPair(":", null, false, 0xFFFFFF00), new SpecialPair("2>", null, false, 0), new SpecialPair("&1", null, false, 0) };
    private static SpecialPair[] sConfSpecialPairs = {
        new SpecialPair("[", "]", true, 0xFFFFFF00) };
    private static SpecialPair[] sXmlSpecialPairs = {
        new SpecialPair("\"", "\"", true, 0xFFFF00FF), new SpecialPair("'", "'", true, 0xFFFF00FF), new SpecialPair("<!--", "-->", true, 0xFF00FF00) };
    
    private static String[] sCppKeywords =
        { "alignas", "alignof", "and", "and_eq", "asm", "auto", "bitand", "bitor",
          "bool", "break", "case", "catch", "char", "char16_t", "char32_t", "class",
          "compl", "const", "constexpr", "const_cast", "continue", "decltype", "default",
          "delete", "do", "double", "dynamic_cast", "else", "enum", "explicit", "export",
          "extern", "float", "for", "friend", "goto", "if", "inline", "int", "long",
          "mutable", "namespace", "new", "noexcept", "not", "not_eq", "operator",
          "or", "or_eq", "private", "protected", "public", "register", "reinterpret_cast",
          "return", "short", "signed", "sizeof", "static", "static_assert", "static_cast",
          "struct", "switch", "template", "this", "thread_local", "throw", "try",
          "typedef", "typeid", "typename", "union", "unsigned", "using", "virtual", "void",
          "volatile", "wchar_t", "while", "xor", "xor_eq" };
    private static String[] sJavaKeywords =
        { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
          "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
          "goto", "if", "implements", "import", "instanceof", "int", "interface",
          "long", "native", "new", "package", "private", "protected",
          "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
          "throw", "throws", "transient", "try", "void", "volatile", "while" };
    private static String[] sBatchKeywords =
        { "assoc", "break", "call", "cd", "chdir", "cls", "color", "copy", "date", "del", "dir",
          "echo", "endlocal", "erase", "exit", "for", "ftype", "goto", "graftabl", "if", "md", "mkdir",
          "mklink", "move", "path", "pause", "popd", "prompt", "pushd", "rd", "rem", "ren", "rename",
          "rmdir", "set", "setlocal", "shift", "start", "time", "title", "type", "ver", "verify", "vol" };
          
    private static Scheme mSchemeCPP   = new Scheme("C/C++", true,  "_",  "_",  "#",  "//",   sCppKeywords,   sCppLiterals,   sCppSpecialPairs);
    private static Scheme mSchemeJava  = new Scheme("Java",  true,  "_",  "_",  "@",  "//",   sJavaKeywords,  sJavaLiterals,  sCppSpecialPairs);
    private static Scheme mSchemeBatch = new Scheme("Batch", false, "_",  "_",  null, "rem ", sBatchKeywords, sBatchLiterals, sBatchSpecialPairs);
    private static Scheme mSchemeConf  = new Scheme("Conf",  true,  null, null, null, "#",    null,           null,           sConfSpecialPairs);
    private static Scheme mSchemeXml   = new Scheme("Xml",   true,  null, ".:", null, null,   null,           null,           sXmlSpecialPairs);
        
    static Scheme getScheme(android.net.Uri uri)
    {
        if (uri == null)
            return null;
        String ext = Utils.getFileExtension(uri).toLowerCase();;
        switch (ext)
        {
        case "cpp":
        case "cxx":
        case "cc":
        case "c":
        case "h":
            return mSchemeCPP;
            
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
    
    private java.util.Comparator<CharSequence> mComp; 
    
    SyntaxHighlighter(Editable e, Scheme scheme)
    {
        mEditable = e;
        mScheme = scheme;
        
        mComp = mScheme.caseSensitive ? new CharSequenceUtils.Comparator() : new CharSequenceUtils.ComparatorIgnoreCase();
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
        
        Tokenizer t = new Tokenizer(mEditable.subSequence(start, end), mComp);
        t.mTokenStart = mScheme.tokenStart;
        t.mTokenChars = mScheme.tokenChars;
        t.mLineComment = mScheme.lineComment;
        t.mSpecials = mScheme.getSpecials();
        if (t.mSpecials != null)
            java.util.Arrays.sort(t.mSpecials);
        CharSequence lastToken = null;
        
        boolean cont = true;
        while (cont)
        {
            Tokenizer.Type tt = t.getNextToken(true);
            
            Object span = null;
            int spanstart = t.getStart() + start;
            int spanend = t.getEnd() + start;
            switch (tt)
            {
            case UNKNOWN:
                if (mScheme.preProcessor != null && mComp.compare(t.get(), mScheme.preProcessor) == 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFF6820);
                break;
                    
            case TOKEN:
                if (mScheme.preProcessor != null && mComp.compare(lastToken, mScheme.preProcessor) == 0)
                    span = new android.text.style.ForegroundColorSpan(0xFFFF6820);
                else if (mScheme.keywords != null && java.util.Arrays.binarySearch(mScheme.keywords, t.get(), mComp) >= 0)
                    span = new android.text.style.ForegroundColorSpan(0xFF00FFFF);
                else if (mScheme.literals != null && java.util.Arrays.binarySearch(mScheme.literals, t.get(), mComp) >= 0)
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
                    SpecialPair sp = mScheme.findSpecialPairBegin(t.get(), mComp);
                    if (sp != null)
                    {
                        if (findSpecial(t, sp.end, sp.skipws))
                        {
                            spanend = t.getEnd() + start - 1;
                            cont = false;
                        }
                        else
                        {
                            spanend = t.getEnd() + start;
                        }
                        if (sp.color != 0)
                            span = new android.text.style.ForegroundColorSpan(sp.color);
                    }
                }
                break;
                
            case END:
                cont = false;
                break;
            }
            if (span != null)
                mEditable.setSpan(span, spanstart, spanend, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            lastToken = t.get();
        }
    }
    
    private boolean findSpecial(Tokenizer t, CharSequence sp, boolean skipws)
    {
        boolean cont = true;
        boolean endtoken = false;
        t.mLineComment = null;
        while (cont)
        {
            Tokenizer.Type tt = t.getNextToken(skipws);
            switch (tt)
            {
            case WHITE_SPACE:
                cont = false;
                break;
                
            case SPECIAL:
                cont = mComp.compare(t.get(), sp) != 0;
                break;
                
            case END:
                endtoken = true;
                cont = false;
                break;
            }
        }
        t.mLineComment = mScheme.lineComment;
        return endtoken;
    }
}
