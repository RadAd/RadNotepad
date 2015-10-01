package au.radsoft.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

//import android.util.Log;

import au.radsoft.R;
import static au.radsoft.utils.CharSequenceUtils.*;

public class TextSearchView extends SearchView implements SearchView.OnQueryTextListener, View.OnClickListener
{
    private static String _tag = "TextSearchView";
    
    private EditText mTextView = null;
    private Object mSpan = null;
    
    public TextSearchView(Context context)
    {
        super(context);
        setOnQueryTextListener(this);
        addExtraButtons();
    }

    public TextSearchView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnQueryTextListener(this);
        addExtraButtons();
    }
    
    private void addExtraButtons()
    {
        LinearLayout l = (LinearLayout) getChildAt(0);
 
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View v = layoutInflater.inflate(R.layout.search_view_buttons, l, false);
        l.addView(v);
        
        View back = findViewById(R.id.search_back);
        View forward = findViewById(R.id.search_forward);
        
        back.setOnClickListener(this);
        forward.setOnClickListener(this);
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
    
    @Override // from View.OnClickListener
    public void onClick(View v)
    {
        CharSequence query = getQuery();
        if (query.length() > 0)
        {
            switch (v.getId())
            {
            case R.id.search_forward:
                if (!findHighlight(query, mTextView.getSelectionEnd(), true))
                {
                    toast("'%s' not found", query);
                }
                break;
                
            case R.id.search_back:
                if (!findHighlight(query, mTextView.getSelectionStart() - query.length(), false))
                {
                    toast("'%s' not found", query);
                }
                break;
            }
        }
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
        //Log.i(_tag, String.format("onQueryTextChange: '%s'", newText));
        findHighlight(newText, mTextView.getSelectionStart(), true);
        return true;
    }
    
    @Override // SearchView.OnQueryTextListener
    public boolean onQueryTextSubmit(String query)
    {
        //Log.i(_tag, String.format("onQueryTextSubmit: '%s'", query));
        if (!findHighlight(query, mTextView.getSelectionEnd(), true))
        {
            toast("'%s' not found", query);
        }
        return true;
    }
    
    private boolean findHighlight(CharSequence query, int o, boolean forwards)
    {
        CharSequence text = mTextView.getText();
        int i = -1;
        if (forwards)
        {
            i = indexOfIgnoreCase(text, query, o);
            if (i == -1 && o != 0)
                i = indexOfIgnoreCase(text, query);
        }
        else
        {
            i = lastIndexOfIgnoreCase(text, query, o);
            if (i == -1)
                i = lastIndexOfIgnoreCase(text, query);
        }
        
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
    
    private void toast(String fmt, Object... args)
    {
        String msg = String.format(fmt, args);
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
