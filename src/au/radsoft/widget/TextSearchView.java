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

public class TextSearchView extends SearchView implements SearchView.OnQueryTextListener, View.OnClickListener, View.OnLongClickListener
{
    //private static String TAG = TextSearchView.class.getCanonicalName();
    
    private EditText mTextView = null;
    private Object mSpan = null;
    private boolean caseSensitive_ = false;
    
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
 
        if (l !=null)
        {
            
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View v = layoutInflater.inflate(R.layout.search_view_buttons, l, false);
            l.addView(v);
        }
        
        setOnClickListener(R.id.search_back);
        setOnClickListener(R.id.search_forward);
        setOnClickListener(R.id.search_case);
        
        updateCaseSensitiveButton();
    }
    
    private void setOnClickListener(int r)
    {
        View v = findViewById(r);
        if (v != null)
        {
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }
    }
    
    private void updateCaseSensitiveButton()
    {
        android.widget.ImageView caseiv = (android.widget.ImageView) findViewById(R.id.search_case);
        if (caseiv != null)
            caseiv.setImageAlpha(caseSensitive_ ? 255 : 130);
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
                if (!findHighlight(query, mTextView.getSelectionStart(), false))
                {
                    toast("'%s' not found", query);
                }
                break;
                
            case R.id.search_case:
                caseSensitive_ = !caseSensitive_;
                updateCaseSensitiveButton();
                break;
            }
        }
    }
    
    @Override // from View.OnLongClickListener
    public boolean onLongClick(View v)
    {
        CharSequence d = v.getContentDescription();
        if (d != null)
            toast(d.toString());
        return true;
    }
    
    @Override
    public void onActionViewExpanded()
    {
        View rv = getRootView();
        View sv = findViewById(R.id.search_view_buttons);
        boolean showExtraButtons = sv != null && rv.getWidth() > 1500;
        if (sv != null)
            sv.setVisibility(showExtraButtons ? View.VISIBLE : View.GONE);
        setSubmitButtonEnabled(!showExtraButtons);
        //toast("width " + rv.getWidth());
        
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
            i = indexOf(text, query, o, caseSensitive_);
            if (i == -1 && o != 0)
                i = indexOf(text, query, caseSensitive_);
        }
        else
        {
            i = lastIndexOf(text, query, o - query.length(), caseSensitive_);
            if (i == -1)
                i = lastIndexOf(text, query, caseSensitive_);
        }
        
        if (i >= 0)
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
    
    private void toast(int fmt, Object... args)
    {
        getResources().getString(fmt, args);
        String msg = String.format(fmt, args);
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }
    
    private void toast(String fmt, Object... args)
    {
        String msg = String.format(fmt, args);
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
