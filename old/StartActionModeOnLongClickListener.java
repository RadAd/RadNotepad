package au.radsoft;

import android.view.ActionMode;
import android.view.View;
import android.widget.TextView;

public class StartActionModeOnLongClickListener implements View.OnLongClickListener
{
    public static void init(TextView tv)
    {
        tv.setOnLongClickListener(new StartActionModeOnLongClickListener());
    }
    
    @Override
    public boolean onLongClick(View view)
    {
        if (view instanceof TextView)
        {
            TextView tv = (TextView) view;
            ActionMode.Callback cb = tv.getCustomSelectionActionModeCallback();
            if (cb != null)
            {
                /*mActionMode =*/ tv.startActionMode(cb);
                tv.setSelected(true);
            }
            return true;
        }
        else
            return false;
    }
}
