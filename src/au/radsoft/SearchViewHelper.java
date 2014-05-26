package au.radsoft;

import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import  android.util.Log;

class SearchViewHelper implements SearchView.OnQueryTextListener
{
    private static String _tag = "SearchViewHelper";
    
    private EditText mEditText;
    private Object mSpan;
    
    static SearchViewHelper attach(SearchView searchView, EditText editText)
    {
        SearchViewHelper searchViewHelper = new SearchViewHelper(editText);
        searchView.setOnQueryTextListener(searchViewHelper);
        return searchViewHelper;
    }
    
    SearchViewHelper(EditText editText)
    {
        mEditText = editText;
        
        //mSpan = new android.text.style.StyleSpan(android.graphics.Typeface.BOLD);
        //mSpan = new android.text.style.ForegroundColorSpan(0xFF0000);
        mSpan = new android.text.style.BackgroundColorSpan(editText.getResources().getColor(android.R.color.holo_blue_light));
    }
    
    @Override
    public boolean onQueryTextChange(String newText)
    {
        Log.i(_tag, String.format("onQueryTextChange: '%s'", newText));
        findHighlight(newText, mEditText.getSelectionStart());
        // TODO text gets set to empty when searchview is closed
        // TODO Need to override SearchView.onActionViewCollapsed to call onCloseListener
        return true;
    }
    
    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.i(_tag, String.format("onQueryTextSubmit: '%s'", query));
        if (!findHighlight(query, mEditText.getSelectionEnd()))
        {
            String msg = String.format("'%s' not found", query);
            Toast toast = Toast.makeText(mEditText.getContext(), msg, Toast.LENGTH_LONG);
            toast.show();
        }
        return true;
    }
    
    private boolean findHighlight(String query, int o)
    {
        int i = Utils.find(mEditText.getText(), o, query);
        if (i == -1 && o != 0)
            i = Utils.find(mEditText.getText(), 0, query);
        if (i != -1)
        {
            mEditText.setSelection(i, i + query.length());
            mEditText.getText().setSpan(mSpan, i, i + query.length(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //mEditText.requestFocus();
            //menuSearch.collapseActionView();
            return true;
        }
        else
        {
            mEditText.setSelection(o);
            mEditText.getText().removeSpan(mSpan);
            return false;
        }
    }
}
