package au.radsoft.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.ScrollView;

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

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        if (listeners != null)
        {
            for (SelectionChangedListener l : listeners)
                l.onSelectionChanged(selStart, selEnd);        
        }
        
        ScrollView parent = (ScrollView) getParent();
        if (parent != null)
        {
            Layout layout = getLayout();
            if (layout != null)
            {
                int line = layout.getLineForOffset(selStart);
                Rect r = new Rect();
                layout.getLineBounds(line, r);
                r.left = (int) layout.getPrimaryHorizontal(selStart);
                if (r.top < parent.getScrollY())
                    parent.smoothScrollTo(r.left, r.top);
                else if (r.bottom > (parent.getScrollY() + parent.getHeight()))
                    parent.smoothScrollTo(r.left, r.bottom - parent.getHeight());
            }
        }
    }
}
