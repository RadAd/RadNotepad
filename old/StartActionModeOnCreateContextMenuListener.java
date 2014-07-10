package au.radsoft;

import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

public class StartActionModeOnCreateContextMenuListener implements View.OnCreateContextMenuListener
{
    public static void init(TextView tv)
    {
        tv.setOnCreateContextMenuListener(new StartActionModeOnCreateContextMenuListener());
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
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
        }
    }
}
