package au.radsoft.widget;

import android.content.Context;
import android.util.AttributeSet;

import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;

import static au.radsoft.utils.CharSequenceUtils.*;

public class TextSearchView extends SearchView implements SearchView.OnQueryTextListener
{
    private static String _tag = "TextSearchView";
    
    private EditText mTextView = null;
    private Object mSpan = null;
    
    public TextSearchView(Context context)
    {
        super(context);
        setOnQueryTextListener(this);
    }

    public TextSearchView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnQueryTextListener(this);
    }
    
    public void attach(EditText textView)
    {
        mTextView = textView;
        
        if (mTextView != null)
            //mSpan = new android.text.style.StyleSpan(android.graphics.Typeface.BOLD);
            //mSpan = new android.text.style.ForegroundColorSpan(0xFF0000);
            mSpan = new android.text.style.BackgroundColorSpan(mTextView.getResources().getColor(android.R.color.holo_blue_light));
        else
            mSpan = null;
    }
    
    @Override
    public void onActionViewExpanded()
    {
        if (mTextView != null)
        {
            // super.onActionViewExpanded() seems to clear selected text
            int st = mTextView.getSelectionStart();
            int en = mTextView.getSelectionEnd();
            
            super.onActionViewExpanded();
            
            if (st != en)
            {
                mTextView.setSelection(st, en);
                CharSequence sel = mTextView.getText().subSequence(st, en);
                setQuery(sel, false);
            }
        }
        else
            super.onActionViewExpanded();
    }
    
    @Override
    public void onActionViewCollapsed()
    {
        if (mTextView != null)
        {
            // super.onActionViewCollapsed() seems to clear selected text
            int st = mTextView.getSelectionStart();
            int en = mTextView.getSelectionEnd();
            
            super.onActionViewCollapsed();
            
            if (st != en)
                mTextView.setSelection(st, en);
			mTextView.getText().removeSpan(mSpan);
        }
        else
            super.onActionViewCollapsed();
    }
    
    @Override // SearchView.OnQueryTextListener
    public boolean onQueryTextChange(String newText)
    {
        Log.i(_tag, String.format("onQueryTextChange: '%s'", newText));
        findHighlight(newText, mTextView.getSelectionStart());
        return true;
    }
    
    @Override // SearchView.OnQueryTextListener
    public boolean onQueryTextSubmit(String query)
    {
        Log.i(_tag, String.format("onQueryTextSubmit: '%s'", query));
        if (!findHighlight(query, mTextView.getSelectionEnd()))
        {
            String msg = String.format("'%s' not found", query);
            Toast toast = Toast.makeText(mTextView.getContext(), msg, Toast.LENGTH_LONG);
            toast.show();
        }
        return true;
    }
    
    private boolean findHighlight(String query, int o)
    {
        int i = find(mTextView.getText(), o, query);
        if (i == -1 && o != 0)
            i = find(mTextView.getText(), 0, query);
        if (i != -1)
        {
            mTextView.setSelection(i, i + query.length());
            mTextView.getText().setSpan(mSpan, i, i + query.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return true;
        }
        else
        {
            mTextView.setSelection(o);
            mTextView.getText().removeSpan(mSpan);
            return false;
        }
    }
}
