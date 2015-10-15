package au.radsoft;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.widget.TextView;

// TODO
// Speed improvement by combining spans

public class UnprintableWatcher implements TextWatcher
{
    private static final String TAG = MainActivity.class.getCanonicalName();
    
    private static final String[] C0 = { "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",
                                         "BS",  "HT",  "LF",  "VT",  "FF",  "CR",  "SO",  "SI",
                                         "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
                                         "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US"  };
    private static final String[] C1 = { "PAD", "HOP", "BPH", "NBH", "IND", "NEL", "SSA", "ESA",
                                         "HTS", "HTJ", "VTS", "PLD", "PLU", "RI",  "SS2", "SS3",
                                         "DCS", "PU1", "PU2", "STS", "CCH", "MW",  "SPA", "EPA",
                                         "SOS", "SGCI","SCI", "CSI", "ST",  "OSC", "PM",  "APC" };
                                   
    // alternatives
    // \u2420 // Symbol for space
    // \u2423 // OpenBox
    // \u00B7 // Middle dot
    private static final String SPACES = "\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7\u00B7";
    private static final String TABS = " \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5 \u21E5";

    private static class UnprintableSpan extends ReplacementSpan
    {
        private final String mText;
        private final int mColor;
        
        private final Rect mRect = new Rect();
        private final Rect mRectOther = new Rect();
        
        private UnprintableSpan(String text, int color)
        {
            mText = text;
            mColor = color;
        }
        
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
        {
            paint.getTextBounds(mText, 0, mText.length(), mRect);
            mRect.inset(-1, -1);
            return (int) (paint.measureText(mText) + 0.5);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
        {
            //paint = new Paint(paint);
            mRectOther.set(mRect);
            mRectOther.offset((int) x, y);
            canvas.drawRect(mRectOther, paint);
            
            int c = paint.getColor();
            paint.setColor(mColor);
            canvas.drawText(mText, x, y, paint);
            paint.setColor(c);
        }
    }
    
    private static class ShowSpaceSpan extends ReplacementSpan
    {
        private final String mText;
        
        private ShowSpaceSpan(String text)
        {
            mText = text;
        }
        
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
        {
            return (int) (paint.measureText(text, start, end) + 0.5);
            //return (int) (paint.measureText(mText, 0, end - start) + 0.5);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
        {
            //paint = new Paint(paint);
            int c = paint.getColor();
            paint.setAlpha(128);
            canvas.drawText(mText, x, y, paint);
            //canvas.drawText(mText, 0, end - start, x, y, paint);
            paint.setColor(c);
        }
    }
    
    private final TextView mTextView;
    private int mColor = 0xffff0000;
    private boolean mShowSpace = false;
    private boolean mShowUnprintable = true;

    public UnprintableWatcher(TextView textView)
    {
        mTextView = textView;
        mTextView.addTextChangedListener(this);
    }
    
    public void setColor(int color)
    {
        mColor = color;
    }
    
    public void showWhitespace(boolean showWhitespace)
    {
        if (showWhitespace != mShowSpace)
        {
            Spannable spannable = (Spannable) mTextView.getText();
            mShowSpace = showWhitespace;
            if (mShowSpace)
                addSpaces(spannable, 0, spannable.length());
            else
                removeSpaces(spannable, 0, spannable.length());
        }
    }
    
    public void showUnprintable(boolean showUnprintable)
    {
        if (showUnprintable != mShowUnprintable)
        {
            Spannable spannable = (Spannable) mTextView.getText();
            mShowUnprintable = showUnprintable;
            if (mShowUnprintable)
                addUnprintable(spannable, 0, spannable.length(), mColor);
            else
                removeUnprintable(spannable, 0, spannable.length());
        }
    }
    
    @Override //TextWatcher
    public void afterTextChanged(Editable s) { }
    
    @Override //TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    
    @Override //TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        int end = start + count;
        long startTime = System.currentTimeMillis();
        Log.d(TAG, "+onTextChanged " + s.subSequence(start, end));
        
        Spannable spannable = (Spannable) s;
        
        if (mShowSpace)
            addSpaces(spannable, start, end);

        if (mShowUnprintable)
            addUnprintable(spannable, start, end, mColor);
        
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "-onTextChanged " + (endTime - startTime)/1000.0f);
    }
    
    private static void addUnprintable(Spannable spannable, int start, int end, int color)
    {
        for (int i = start; i < end; ++i)
        {
            ReplacementSpan span = null;
            char c = spannable.charAt(i);
            
            if (c == '\r' || c == '\n' || c == '\t')
            {
                // do nothing
            }
            else if (c >= 0x00 && (c - 0x00) < C0.length)
            {
                span = new UnprintableSpan(C0[c - 0x00], color);
            }
            else if (c >= 0x80 && (c - 0x80) < C1.length)
            {
                span = new UnprintableSpan(C1[c - 0x80], color);
            }
            //else if (Character.isISOControl(c))
            //{
                //span = new UnprintableSpan('u' + Integer.toString(c, 16), color);
            //}
            
            if (span != null)
            {
                Log.d(TAG, " span " + i + " " + (int)c);
                spannable.setSpan(span, i, i + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    
    private static void removeUnprintable(Spannable spannable, int start, int end)
    {
        UnprintableSpan[] spans = spannable.getSpans(start, end, UnprintableSpan.class);
        for (UnprintableSpan s : spans)
            spannable.removeSpan(s);
    }
            
    private static void addSpaces(Spannable spannable, int start, int end)
    {
        for (int i = start; i < end; ++i)
        {
            ReplacementSpan span = null;
            char c = spannable.charAt(i);
            
            if (c == '\r' || c == '\n')
            {
                // do nothing
            }
            else if (c == '\t')
            {
                span = new ShowSpaceSpan(" \u21E5");
            }
            else if (Character.isSpaceChar(c))
            {
                span = new ShowSpaceSpan("\u00B7");
            }
            
            if (span != null)
            {
                Log.d(TAG, " span " + i + " " + (int)c);
                spannable.setSpan(span, i, i + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    
    private static void removeSpaces(Spannable spannable, int start, int end)
    {
        ShowSpaceSpan[] spans = spannable.getSpans(start, end, ShowSpaceSpan.class);
        for (ShowSpaceSpan s : spans)
            spannable.removeSpan(s);
    }
}
