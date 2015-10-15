package au.radsoft;

import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Range;
import radsoft.syntaxhighlighter.brush.Brush;
import radsoft.syntaxhighlighter.SyntaxHighlighter;

public class SyntaxHighlighterWatcher implements TextWatcher
{
    private Brush mBrush = SyntaxHighlighter.getBrushByName("");
    private Theme mTheme = null;
    private TextView mTextView;

    public SyntaxHighlighterWatcher(TextView textView, String theme)
    {
        mTextView = textView;
        mTextView.addTextChangedListener(this);
        
        setThemeByName(theme);
    }
    
    String getBrushName()
    {
        return mBrush.getName();
    }
    
    void setBrushByExtension(String fileExtension)
    {
        mBrush = SyntaxHighlighter.getBrushByExtension(fileExtension);
        highlightSyntax((Spannable) mTextView.getText(), mBrush, mTheme, 0, mTextView.getText().length());
    }
    
    void setBrushByName(String name)
    {
        if (getBrushName().equals(name))
            return;
        
        mBrush = SyntaxHighlighter.getBrushByName(name);
        highlightSyntax((Spannable) mTextView.getText(), mBrush, mTheme, 0, mTextView.getText().length());
    }
    
    void setThemeByName(String name)
    {
        if (mTheme != null && mTheme.getName().equals(name))
            return;
        
        mTheme = Theme.getThemeByName(name);
        mTextView.setTextColor(mTheme.getFgColor());
        mTextView.setBackgroundColor(mTheme.getBgColor());
        mTextView.setHighlightColor(mTheme.getHlColor());
        highlightSyntax((Spannable) mTextView.getText(), mBrush, mTheme, 0, mTextView.getText().length());
    }
    
    int getBackgroundColor()
    {
        return mTheme.getBgColor();
    }
    
    @Override //TextWatcher
    public void afterTextChanged(Editable s) { }
    
    @Override //TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    
    @Override //TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        Layout layout = mTextView.getLayout();
        if (layout != null)
        {
            int lineBegin = layout.getLineForOffset(start);
            int lineEnd = layout.getLineForOffset(start + count);
            
            lineBegin = Math.max(lineBegin - 10, 0);
            lineEnd = Math.min(lineEnd + 10, layout.getLineCount() - 1);
            
            int lineBeginOffset = layout.getLineStart(lineBegin);
            int lineEndOffset = layout.getLineEnd(lineEnd);
            
            highlightSyntax((Spannable) s, mBrush, mTheme, lineBeginOffset, lineEndOffset);
        }
    }
    
    private static void highlightSyntax(Spannable spannable, Brush brush, Theme theme, int start, int end)
    {
        if (brush != null)
        {
            remove(spannable, start, end);
           
            SyntaxHighlighter sh = new SyntaxHighlighter(brush);
            Map<Range<Integer>, String> regionList = sh.parse(spannable, start, end);
            for (Map.Entry<Range<Integer>, String> r : regionList.entrySet())
            {
                Object[] spans = theme.getSpans(r.getValue());
                for (Object span : spans)
                    spannable.setSpan(span, r.getKey().lowerEndpoint(), r.getKey().upperEndpoint(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        else
        {
            remove(spannable, 0, spannable.length());
        }
    }
    
    private static void remove(Spannable spannable, int start, int end)
    {
        for (Class<? extends Object> c : Theme.getSpanTypes())
        {
            Object[] spans = spannable.getSpans(start, end, c);
            for (Object s : spans)
                spannable.removeSpan(s);
        }
    }
    
    static Set<String> getBrushList()
    {
        Set<String> brushNames = new java.util.TreeSet<String>();
        
        for (Brush brush : SyntaxHighlighter.getBrushes().values())
        {
            brushNames.add(brush.getName());
        }
        
        return brushNames;
    }
}
