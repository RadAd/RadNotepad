package au.radsoft;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.widget.TextView;

public class UnprintableWatcher implements TextWatcher
{
    private static String[] C0 = { "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",
                                   "BS",  "HT",  "LF",  "VT",  "FF",  "CR",  "SO",  "SI",
                                   "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
                                   "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US"  };
    private static String[] C1 = { "PAD", "HOP", "BPH", "NBH", "IND", "NEL", "SSA", "ESA",
                                   "HTS", "HTJ", "VTS", "PLD", "PLU", "RI",  "SS2", "SS3",
                                   "DCS", "PU1", "PU2", "STS", "CCH", "MW",  "SPA", "EPA",
                                   "SOS", "SGCI","SCI", "CSI", "ST",  "OSC", "PM",  "APC" };
                                 
    private static class UnprintableSpan extends ReplacementSpan
    {
        private final String mText;
        private final Rect mRect = new Rect();
        private final int mColor;
        private int mWidth = -1;
        
        private UnprintableSpan(String text, int color)
        {
            mText = text;
            mColor = color;
        }
        
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
        {
            if (mWidth < 0)
            {
                paint.getTextBounds(mText, 0, mText.length(), mRect);
                mRect.inset(-1, -1);
                mWidth = (int) (paint.measureText(mText) + 0.5);
            }
            return mWidth;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
        {
            paint = new Paint(paint);
            Rect r = new Rect(mRect);
            r.offset((int) x, y);
            canvas.drawRect(r, paint);
            paint.setColor(mColor);
            canvas.drawText(mText, x, y, paint);
        }
    }
    
    private TextView mTextView;
    private int mColor = 0xffff0000;

    public UnprintableWatcher(TextView textView)
    {
        mTextView = textView;
        mTextView.addTextChangedListener(this);
    }
    
    public void setColor(int color)
    {
        mColor = color;
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
            
            showUnprintable((Spannable) mTextView.getText(), lineBeginOffset, lineEndOffset, mColor);
        }
    }
    
    private static void showUnprintable(Spannable spannable, int start, int end, int color)
    {
        remove(spannable, start, end);
        
        for (int i = start; i < end; ++i)
        {
            String n = null;
            char c = spannable.charAt(i);
            if (c >= 0x00 && (c - 0x00) < C0.length)
                n = C0[c - 0x00];
            if (c >= 0x80 && (c - 0x80) < C1.length)
                n = C1[c - 0x80];
            else if (c == '\t')
                n = null;
            //else if (Character.isISOControl(c))
                //n = 'u' + Integer.toString(c, 16);
            
            if (n != null)
            {
                Object span = new UnprintableSpan(n, color);
                spannable.setSpan(span, i, i + 1, android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
    }
    
    private static void remove(Spannable spannable, int start, int end)
    {
        Object[] spans = spannable.getSpans(start, end, UnprintableSpan.class);
        for (Object s : spans)
            spannable.removeSpan(s);
    }
}
