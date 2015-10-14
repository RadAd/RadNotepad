package au.radsoft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.text.style.TabStopSpan;
import android.text.Editable;
import android.text.Layout;

import java.io.File;

import au.radsoft.widget.TextSearchView;
import au.radsoft.widget.EditText;
import au.radsoft.preferences.PreferenceActivity;

// TODO
// Close search view when lose focus
// Support for links

public class MainActivity extends Activity implements EditText.SelectionChangedListener, UndoRedoHelper.HistoryChangedListener, ActionMode.Callback, SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = MainActivity.class.getCanonicalName();

    static final int ACTIVITY_OPEN_FILE = 1;
    static final int ACTIVITY_SAVE_FILE = 2;

    public static final int PREF_FONT_SIZE_DEFAULT = 10;
    public static final int PREF_TAB_SIZE_DEFAULT = 4;
    public static final String PREF_FONT_SIZE = "pref_font_size";
    public static final String PREF_FONT_FILE = "pref_font_file";
    public static final String PREF_FONT_THEME = "pref_font_theme";
    public static final String PREF_TAB_SIZE = "pref_tab_size";
    
    static final int GROUP_SCHEME = 100;

    EditText mEdit;

    TextView mStatusBrush;
    TextView mStatusEncoding;
    TextView mStatusLineEnding;
    TextView mStatusCursor;

    ActionMode mActionMode = null;
    ShareActionProvider myShareActionProvider = null;
    SubMenu mMenuScheme = null;

    UndoRedoHelper mUndoRedoHelper;
    SyntaxHighlighterWatcher mSyntaxHighlighterWatcher;
    boolean mWordWrap = false;
    TextFile mTextFile = new TextFile();
    long mLastModified = -1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        SharedPreferences sharedPref = PreferenceActivity.init(this, R.xml.preferences);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        setContentView(R.layout.main);

        mEdit = (EditText) findViewById(R.id.edit);
        mEdit.setHorizontallyScrolling(!mWordWrap); // bug when set in xml
        mUndoRedoHelper = new UndoRedoHelper(mEdit);
        mUndoRedoHelper.addHistoryChangedListener(this);
        mSyntaxHighlighterWatcher = new SyntaxHighlighterWatcher(mEdit, sharedPref.getString(PREF_FONT_THEME, ""));
        mEdit.addSelectionChangedListener(this);
        mEdit.setCustomSelectionActionModeCallback(this);
        registerForContextMenu(mEdit);

        mStatusBrush = (TextView) findViewById(R.id.brush);
        mStatusEncoding = (TextView) findViewById(R.id.encoding);
        mStatusLineEnding = (TextView) findViewById(R.id.line_ending);
        mStatusCursor = (TextView) findViewById(R.id.cursor);

        onSelectionChanged(mEdit.getSelectionStart(), mEdit.getSelectionEnd());

        Intent intent = getIntent();
        if (intent != null)
        {
            Uri uri = intent.getData();
            if (uri != null)
            {
                setUri(uri);
                open();
            }
        }
        
        onSharedPreferenceChanged(sharedPref, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);
        mUndoRedoHelper.onSaveInstanceState(state);
        state.putParcelable(TAG + ".file", mTextFile);
        state.putBoolean(TAG + ".wordwrap", mWordWrap);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state)
    {
        super.onRestoreInstanceState(state);
        mUndoRedoHelper.onRestoreInstanceState(state);
        mTextFile = Utils.ifNull((TextFile) state.getParcelable(TAG + ".file"), mTextFile);
        mLastModified = Utils.getLastModified(mTextFile.mUri);
        mWordWrap = state.getBoolean(TAG + ".wordwrap");

        setUri(mTextFile.mUri);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateStatusLineEnding();
        updateStatusFileEncoding();

        if (mLastModified != Utils.getLastModified(mTextFile.mUri))
        {
            new AlertDialog.Builder(this)
                .setTitle("Changed?")
                .setMessage("The file has changed do you wish to revert to saved?")
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            mUndoRedoHelper.markSaved(false);
                        }
                    })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            open();
                        }
                    })
                .create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
        case ACTIVITY_OPEN_FILE:
            if (resultCode == RESULT_OK)
            {
                Uri uri = data.getData();
                if (uri == null)
                    toast(R.string.invalid_file);
                else
                {
                    setUri(uri);
                    open();
                }
            }
            break;

        case ACTIVITY_SAVE_FILE:
            if (resultCode == RESULT_OK)
            {
                Uri uri = data.getData();
                if (uri == null)
                    toast(R.string.invalid_file);
                else
                {
                    setUri(uri);
                    save();
                }
            }
            break;
        }
    }

    @Override
    public void onBackPressed()
    {
        checkSave("Do you wish to save before exiting?", new Runnable() {
                public void run()
                {
                    MainActivity.super.onBackPressed();
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);

        mMenuScheme = menu.addSubMenu(R.string.action_scheme);
        mMenuScheme.add(GROUP_SCHEME, GROUP_SCHEME, Menu.NONE, R.string.action_scheme_none);
        java.util.Set<String> schemes = SyntaxHighlighterWatcher.getBrushList();
        for (String scheme : schemes)
            mMenuScheme.add(GROUP_SCHEME, GROUP_SCHEME, Menu.NONE, scheme);
        mMenuScheme.setGroupCheckable(GROUP_SCHEME, true, true);

        myShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
        updateShareActionProvider();

        final MenuItem menuSearch = menu.findItem(R.id.action_search);
        TextSearchView searchView = (TextSearchView) menuSearch.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_query_hint));
        searchView.attach(mEdit);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        try
        {
            switch (item.getItemId())
            {
            case R.id.action_open_content:
                checkSave("Do you wish to save before opening?", new Runnable() {
                        public void run()
                        {
                            openChooser();
                        }
                    });
                break;

            case R.id.action_revert:
                open();
                break;

            case R.id.action_save:
                save();
                break;

            case R.id.action_save_as:
                saveChooser();
                break;

            case R.id.action_details:
                if (mTextFile.mUri != null)
                {
                    Uri uri = mTextFile.mUri;

                    String nl = "\n";
                    StringBuilder msg = new StringBuilder();
                    msg.append("Location: " + uri.toString());
                    msg.append(nl);
                    msg.append("MIME: " + Utils.ifNull(Utils.getMimeType(uri), ""));

                    if (uri.getScheme().equals("file"))
                    {
                        File f = new File(uri.getPath());
                        msg.append(nl);
                        msg.append("Read only: " + Boolean.toString(!f.canWrite()));
                        java.util.Date d = new java.util.Date(f.lastModified());
                        msg.append(nl);
                        msg.append("Last Modified: " + d);
                    }

                    new AlertDialog.Builder(this)
                        .setTitle(uri.getLastPathSegment())
                        .setMessage(msg)
                        .create().show();
                }
                break;

            case R.id.action_open_with:
                if (mTextFile.mUri != null)
                    startActivity(new Intent(Intent.ACTION_VIEW, mTextFile.mUri));
                break;

            case R.id.action_undo:
                mUndoRedoHelper.undo();
                invalidateOptionsMenu();
                break;

            case R.id.action_redo:
                mUndoRedoHelper.redo();
                invalidateOptionsMenu();
                break;

            case R.id.action_wrap:
                mWordWrap = !mWordWrap;
                mEdit.setHorizontallyScrolling(!mWordWrap);
                break;

            case R.id.action_le_windows:
                mTextFile.mLineEnding = TextFile.LE_WINDOWS;
                updateStatusLineEnding();
                mUndoRedoHelper.markSaved(false);
                break;

            case R.id.action_le_unix:
                mTextFile.mLineEnding = TextFile.LE_UNIX;
                updateStatusLineEnding();
                mUndoRedoHelper.markSaved(false);
                break;

            case R.id.action_le_mac:
                mTextFile.mLineEnding = TextFile.LE_MAC;
                updateStatusLineEnding();
                mUndoRedoHelper.markSaved(false);
                break;

            case R.id.action_settings:
                PreferenceActivity.show(this, R.xml.preferences);
                break;

            case R.id.select_all:
                mEdit.selectAll();
                break;

            case R.id.selection_change_case:
                {
                    CharSequence text = getSelectedText();
                    if (text.length() > 0)
                        replaceSelectedText(Character.isUpperCase(text.charAt(0)) ? text.toString().toLowerCase() : text.toString().toUpperCase());
                }
                break;

            case R.id.selection_share:
                sendChooser();
                break;

            case GROUP_SCHEME:
                mSyntaxHighlighterWatcher.setBrushByName(item.getTitle().toString());
                updateStatusBrush();
                break;

            default:
                return false;
            }

            return true;
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            toast(R.string.no_activity_found);
            return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        Uri uri = mTextFile.mUri;

        Enable(menu.findItem(R.id.action_revert), uri != null && (!mUndoRedoHelper.isSaved() || (mLastModified != Utils.getLastModified(uri))));
        Enable(menu.findItem(R.id.action_save), uri != null && !mUndoRedoHelper.isSaved());
        //Enable(menu.findItem(R.id.action_save_as), uri != null);
        Enable(menu.findItem(R.id.action_details), uri != null);
        Enable(menu.findItem(R.id.action_share), uri != null);
        Enable(menu.findItem(R.id.action_open_with), uri != null);
        Enable(menu.findItem(R.id.action_undo), mUndoRedoHelper.getCanUndo());
        Enable(menu.findItem(R.id.action_redo), mUndoRedoHelper.getCanRedo());
        Check(menu.findItem(R.id.action_wrap), mWordWrap);
        switch (mTextFile.mLineEnding)
        {
        case TextFile.LE_WINDOWS:
            Check(menu.findItem(R.id.action_le_windows), true);
            break;

        case TextFile.LE_UNIX:
            Check(menu.findItem(R.id.action_le_unix), true);
            break;

        case TextFile.LE_MAC:
            Check(menu.findItem(R.id.action_le_mac), true);
            break;
        }

        if (mMenuScheme != null)
        {
            String brushName = mSyntaxHighlighterWatcher.getBrushName();
            if (brushName.isEmpty())
                brushName = "None";
            for (int i = 0; i < mMenuScheme.size(); ++i)
            {
                MenuItem mi = mMenuScheme.getItem(i);
                if (mi.getTitle().equals(brushName))
                    mi.setChecked(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override //SelectionChangedListener
    public void onSelectionChanged(int selStart, int selEnd)
    {
        Layout layout = mEdit.getLayout();
        if (layout != null)
        {
            int line = layout.getLineForOffset(selStart);
            int col = selStart - layout.getLineStart(line);
            if (selStart == selEnd)
                mStatusCursor.setText(String.format("%d:%d", line + 1, col + 1));
            else
                mStatusCursor.setText(String.format("%d:%d (%d)", line + 1, col + 1, selEnd - selStart));
        }
        else
        {
            int line = 0;
            int col = selStart;
            if (selStart == selEnd)
                mStatusCursor.setText(String.format("%d:%d", line + 1, col + 1));
            else
                mStatusCursor.setText(String.format("%d:%d (%d)", line + 1, col + 1, selEnd - selStart));
        }
    }

    @Override //UndoRedoHelper.HistoryChangedListener
    public void onHistoryChanged(UndoRedoHelper helper)
    {
        invalidateOptionsMenu();
    }

    @Override //ActionMode.Callback
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        return onOptionsItemSelected(item);
    }

    @Override //ActionMode.Callback
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        MenuInflater inflater = /*mode.*/getMenuInflater();
        inflater.inflate(R.menu.selection, menu);
        //menu.removeItem(android.R.id.selectAll);
        return true;
    }

    @Override //ActionMode.Callback
    public void onDestroyActionMode(ActionMode mode)
    {
        if (mActionMode == mode)
            mActionMode = null;
    }

    @Override //ActionMode.Callback
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        return onPrepareOptionsMenu(menu);
    }
    
    @Override // OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        boolean updateTabs = false;
        
        if (key == null || key.equals(PREF_FONT_SIZE))
        {
            int size = sharedPreferences.getInt(PREF_FONT_SIZE, PREF_FONT_SIZE_DEFAULT);
            mEdit.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PT, size);
            updateTabs = true;
        }
        
        if (key == null || key.equals(PREF_FONT_FILE))
        {
            Typeface typeface = Typeface.MONOSPACE;
            
            String fontFile = sharedPreferences.getString(PREF_FONT_FILE, "");
            if (fontFile != null && !fontFile.isEmpty())
            {
                try
                {
                    typeface = Typeface.createFromFile(fontFile);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    toast(R.string.error_loading_font_file);
                }
            }
            
            mEdit.setTypeface(typeface);
            updateTabs = true;
        }
        
        if (key == null || key.equals(PREF_FONT_THEME))
        {
            mSyntaxHighlighterWatcher.setThemeByName(sharedPreferences.getString(PREF_FONT_THEME, ""));
        }
        
        if (key == null || key.equals(PREF_TAB_SIZE))
        {
            updateTabs = true;
        }
        
        if (updateTabs)
            setTabs(sharedPreferences);
    }

    void checkSave(String msg, final Runnable cb)
    {
        if (!mUndoRedoHelper.isSaved())
        {
            new AlertDialog.Builder(this)
                .setTitle("Save?")
                .setMessage(msg)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            cb.run();
                        }
                    })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            save();
                            cb.run();
                        }
                    })
                .create().show();
        }
        else
            cb.run();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
        if (view == mEdit)
        {
            if (false)  // If use popup menu
            {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.selection, menu);
            }
            else if (true) // If use action mode
            {
                ActionMode.Callback cb = mEdit.getCustomSelectionActionModeCallback();
                if (cb != null && mActionMode == null)
                {
                    mActionMode = mEdit.startActionMode(cb);
                    mEdit.setSelected(true);
                }
            }
        }
        super.onCreateContextMenu(menu, view, menuInfo);
    }
    
    void setTabs(SharedPreferences sharedPreferences)
    {
        toast("settabs");
        Editable e = mEdit.getEditableText();
        for (TabStopSpan.Standard spanTabStop : e.getSpans(0, e.length(), TabStopSpan.Standard.class))
            e.removeSpan(spanTabStop);
        
        int s = sharedPreferences.getInt(PREF_TAB_SIZE, PREF_TAB_SIZE_DEFAULT);
        float w = mEdit.getPaint().measureText("                    ", 0, s);
        for (int i = 0; i < (80/s); ++i)
            e.setSpan(new TabStopSpan.Standard((int)(i*w + 0.5)), 0, e.length(), android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    void updateStatusLineEnding()
    {
        int le = -1;
        switch (mTextFile.mLineEnding)
        {
        case TextFile.LE_WINDOWS:
            le = R.string.status_le_windows_short;
            break;

        case TextFile.LE_UNIX:
            le = R.string.status_le_unix_short;
            break;

        case TextFile.LE_MAC:
            le = R.string.status_le_mac_short;
            break;
        }

        mStatusLineEnding.setText(getString(le));
    }

    void updateStatusFileEncoding()
    {
        mStatusEncoding.setText(mTextFile.mFileEncoding);
    }

    void updateStatusBrush()
    {
        mStatusBrush.setText(mSyntaxHighlighterWatcher.getBrushName());
    }

    static void Enable(MenuItem mi, boolean enable)
    {
        if (mi != null)
        {
            mi.setEnabled(enable);
            android.graphics.drawable.Drawable icon = mi.getIcon();
            if (icon != null)
                icon.setAlpha(enable ? 255 : 130);
        }
    }

    static void Check(MenuItem mi, boolean enable)
    {
        if (mi != null)
        {
            mi.setChecked(enable);
        }
    }

    void updateShareActionProvider()
    {
        Uri uri = mTextFile.mUri;
        if (myShareActionProvider != null)
        {
            if (uri == null)
                myShareActionProvider.setShareIntent(null);
            else
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Utils.ifNull(Utils.getMimeType(uri), "*/*"));
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                myShareActionProvider.setShareIntent(intent);
            }
        }
    }

    void setUri(Uri uri)
    {
        mTextFile.mUri = uri;

        getActionBar().setSubtitle(uri != null ? uri.getLastPathSegment() : null);

        mSyntaxHighlighterWatcher.setBrushByExtension(Utils.getFileExtension(uri));
        updateStatusBrush();

        invalidateOptionsMenu();

        updateShareActionProvider();
    }

    void open()
    {
        mLastModified = Utils.getLastModified(mTextFile.mUri);
        if (mTextFile.mUri != null)
            new LoadAsyncTask().execute(mTextFile);
    }

    void save()
    {
        if (mTextFile.mUri != null)
            new SaveAsyncTask().execute(mTextFile);
        else
            saveChooser();
    }

    void openChooser()
    {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("text/*");
        Intent intent = Intent.createChooser(chooseFile, getString(R.string.open_prompt));
        startActivityForResult(intent, ACTIVITY_OPEN_FILE);
    }

    void saveChooser()
    {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("text/*");
        Intent intent = Intent.createChooser(chooseFile, getString(R.string.save_as_prompt));
        startActivityForResult(intent, ACTIVITY_SAVE_FILE);
    }

    void sendChooser()
    {
        Intent chooseFile = new Intent(Intent.ACTION_SEND);
        chooseFile.putExtra(Intent.EXTRA_TEXT, getSelectedText());
        chooseFile.setType("text/plain");
        Intent intent = Intent.createChooser(chooseFile, getString(R.string.send_prompt));
        startActivity(intent);
    }

    SharedPreferences getDefaultSharedPreferences()
    {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void toast(int fmt, Object... args)
    {
        String msg = getResources().getString(fmt, args);
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    private void toast(String fmt, Object... args)
    {
        String msg = String.format(fmt, args);
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    CharSequence getSelectedText()
    {
        int st = mEdit.getSelectionStart();
        int en = mEdit.getSelectionEnd();
        Editable e = mEdit.getText();
        return e.subSequence(st, en);
    }

    void replaceSelectedText(CharSequence s)
    {
        final int st = mEdit.getSelectionStart();
        final int en = mEdit.getSelectionEnd();
        Editable e = mEdit.getText();
        e.replace(st, en, s);
        mEdit.post(new Runnable() {
                @Override
                public void run()
                {
                    mEdit.setSelection(st, en);
                }
            });
    }

    private class LoadAsyncTask extends ProgressDialogAsyncTask<TextFile, CharSequence[]>
    {
        @Override
        protected CharSequence[] doInBackground(TextFile... tfs)
        {
            CharSequence[] result = new CharSequence[tfs.length];

            for (int i = 0; i < tfs.length; i++)
            {
                mDlg.setMessage("Loading " + tfs[i].mUri.getLastPathSegment());
                try
                {
                    result[i] = tfs[i].load(getContentResolver());
                }
                catch (OutOfMemoryError | java.io.IOException e)
                {
                    mException = e;
                    e.printStackTrace();
                }
                publishProgress((int) ((i + 1.5f)*100f/tfs.length));
            }

            return result;
        }

        @Override
        protected void onPreExecute()
        {
            mDlg = new ProgressDialog(MainActivity.this);
            mDlg.setTitle("Loading...");
            mDlg.setMessage("Loading file");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(CharSequence[] result)
        {
            if (mException != null)
            {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Error")
                    .setMessage(Utils.getMessage(mException))
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
                setUri(null);
                mEdit.setText(null);
            }
            else
                mEdit.setText(result[0]);
            setTabs(getDefaultSharedPreferences());
            updateStatusLineEnding();
            updateStatusFileEncoding();
            mUndoRedoHelper.clearHistory();
            mUndoRedoHelper.markSaved(true);
            mLastModified = Utils.getLastModified(mTextFile.mUri);
            super.onPostExecute(result);
        }
    }

    private class SaveAsyncTask extends ProgressDialogAsyncTask<TextFile, Void>
    {
        @Override
        protected Void doInBackground(TextFile... tfs)
        {
            for (int i = 0; i < tfs.length; i++)
            {
                mDlg.setMessage("Saving " + tfs[i].mUri.getLastPathSegment());
                try
                {
                    tfs[i].save(getContentResolver(), mEdit.getText());
                }
                catch (OutOfMemoryError | java.io.IOException e)
                {
                    mException = e;
                    e.printStackTrace();
                }
                publishProgress((int) ((i + 1.5f)*100f/tfs.length));
            }

            return null;
        }

        @Override
        protected void onPreExecute()
        {
            mDlg = new ProgressDialog(MainActivity.this);
            mDlg.setTitle("Saving...");
            mDlg.setMessage("Saving file");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (mException != null)
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Error")
                    .setMessage(Utils.getMessage(mException))
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
            else
                toast("Saved");
            mUndoRedoHelper.markSaved(true);
            mLastModified = Utils.getLastModified(mTextFile.mUri);
            super.onPostExecute(result);
        }
    }
}
