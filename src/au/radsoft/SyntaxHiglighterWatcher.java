package au.radsoft;

import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.Map;

import com.google.common.collect.Range;
import radsoft.syntaxhighlighter.brush.Brush;
import radsoft.syntaxhighlighter.SyntaxHighlighter;

public class SyntaxHiglighterWatcher implements TextWatcher
{
    private Brush mBrush = null;
    private TextView mTextView;

    public SyntaxHiglighterWatcher(TextView textView)
    {
        mTextView = textView;
        mTextView.addTextChangedListener(this);
    }
    
    String getBrushName()
    {
        return (mBrush != null) ? mBrush.getName() : "";
    }
    
    void setBrush(String fileExtension)
    {
        mBrush = SyntaxHighlighter.getBrush(fileExtension);
    }
    
    @Override //TextWatcher
    public void afterTextChanged(Editable s) { }
    
    @Override //TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    
    @Override //TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        Layout layout = mTextView.getLayout();
        if (layout != null && mBrush != null)
        {
            int lineBegin = layout.getLineForOffset(start);
            int lineEnd = layout.getLineForOffset(start + count);
            
            lineBegin = Math.max(lineBegin - 10, 0);
            lineEnd = Math.min(lineEnd + 10, layout.getLineCount() - 1);
            
            int lineBeginOffset = layout.getLineStart(lineBegin);
            int lineEndOffset = layout.getLineEnd(lineEnd);
            
            highlightSyntax((Spannable) mTextView.getText(), mBrush, lineBeginOffset, lineEndOffset);
        }
        
        //invalidateOptionsMenu();
    }
    
    private static void highlightSyntax(Spannable spannable, Brush brush, int start, int end)
    {
        if (brush != null)
        {
            remove(spannable, start, end);
           
            SyntaxHighlighter sh = new SyntaxHighlighter(brush);
            Map<Range<Integer>, String> regionList = sh.parse(spannable, start, end);
            for (Map.Entry<Range<Integer>, String> r : regionList.entrySet())
            {
                int color = 0;
                switch (r.getValue())
                {
                case Brush.PREPROCESSOR:
                    color = 0xFFFF6820;
                    break;
                    
                case Brush.KEYWORD:
                    color = 0xFF00FFFF;
                    break;
                    
                case Brush.STRING:
                case Brush.VALUE:
                //case Brush.LITERAL:
                    color = 0xFFFF00FF;
                    break;
                    
                //case Brush.LABEL:
                case Brush.COLOR1:
                    color = 0xFFFFFF00;
                    break;
                    
                case Brush.VARIABLE:
                    color = 0xFF007F7F;
                    break;
                    
                case Brush.COMMENTS:
                    color = 0xFF00FF00;
                    break;
                }
                Object span = new android.text.style.ForegroundColorSpan(color);
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
        android.text.style.ForegroundColorSpan[] spans = spannable.getSpans(start, end, android.text.style.ForegroundColorSpan.class);
        for (Object s : spans)
            spannable.removeSpan(s);
    }
}
