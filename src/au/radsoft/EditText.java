package au.radsoft;

import android.content.Context;
import android.util.AttributeSet;

import java.util.List;
import java.util.ArrayList;

public class EditText extends android.widget.EditText
{
    public interface SelectionChangedListener
    {
        public void onSelectionChanged(int selStart, int selEnd);
    }

    private List<SelectionChangedListener> listeners = new ArrayList<SelectionChangedListener>();

    public EditText(Context context)
    {
        super(context);
    }

    public EditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public EditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void addSelectionChangedListener(SelectionChangedListener o)
    {
        listeners.add(o);
    }

    protected void onSelectionChanged(int selStart, int selEnd)
    {
        if (listeners != null)
        {
            for (SelectionChangedListener l : listeners)
                l.onSelectionChanged(selStart, selEnd);        
        }
        
        android.widget.ScrollView parent = (android.widget.ScrollView) getParent();
        if (parent != null)
        {
            android.text.Layout layout = getLayout();
            int line = layout.getLineForOffset(selStart);
            float x = layout.getPrimaryHorizontal(selStart);
            float y = layout.getLineBaseline(line) + layout.getLineAscent(line);
            parent.smoothScrollTo((int) x, (int) y);
        }
    }
}
