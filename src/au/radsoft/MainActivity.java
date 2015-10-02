package au.radsoft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.common.collect.Range;
import radsoft.syntaxhighlighter.brush.Brush;
import radsoft.syntaxhighlighter.SyntaxHighlighter;

import static au.radsoft.utils.CharSequenceUtils.*;

// TODO
// Close search view when lose focus
// Need to call onPrepareOptionsMenu after onRestoreInstanceState

public class MainActivity extends Activity implements EditText.SelectionChangedListener, TextWatcher, ActionMode.Callback
{
    static final int ACTIVITY_OPEN_FILE = 1;
    static final int ACTIVITY_SAVE_FILE = 2;
    
    static final String LE_WINDOWS = "\r\n";
    static final String LE_UNIX = "\n";
    static final String LE_MAC = "\r";
    
    EditText mEdit;
    
    TextView mStatusBrush;
    TextView mEncoding;
    TextView mStatusLineEnding;
    TextView mStatusCursor;
    
    UndoRedoHelper mUndoRedoHelper;    
    ShareActionProvider myShareActionProvider;
    boolean mWordWrap = false;
    String mLineEnding = LE_WINDOWS;
    Brush mBrush = null;
    long mLastModified = -1;
    ActionMode mActionMode = null;
    
    Uri mUri;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mEdit = (EditText) findViewById(R.id.edit);
        mEdit.setHorizontallyScrolling(!mWordWrap); // bug when set in xml
        mUndoRedoHelper = new UndoRedoHelper(mEdit);
        mEdit.addSelectionChangedListener(this);
        mEdit.addTextChangedListener(this);
        mEdit.setCustomSelectionActionModeCallback(this);
        registerForContextMenu(mEdit);
        
        mStatusBrush = (TextView) findViewById(R.id.brush);
        mEncoding = (TextView) findViewById(R.id.encoding);
        mStatusLineEnding = (TextView) findViewById(R.id.line_ending);
        mStatusCursor = (TextView) findViewById(R.id.cursor);
        
        mEncoding.setText("UTF-8");
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
    }

    @Override
    protected void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);
        mUndoRedoHelper.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state)
    {
        super.onRestoreInstanceState(state);
        mUndoRedoHelper.onRestoreInstanceState(state);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        updateStatusLineEnding();
        updateStatusBrush();
        
        if (mLastModified != Utils.getLastModified(mUri))
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
                    toast("Invalid file");
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
                    toast("Invalid file");
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
        inflater.inflate(R.menu.line_ending, menu.addSubMenu(R.string.action_line_ending));
        
        myShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
        updateShareActionProvider();
        
        final MenuItem menuSearch = menu.findItem(R.id.action_search);
        au.radsoft.widget.TextSearchView searchView = (au.radsoft.widget.TextSearchView) menuSearch.getActionView();
        searchView.setQueryHint("Search in file");
        searchView.attach(mEdit);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
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
            if (mUri != null)
            {
                Uri uri = mUri;
                
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
            if (mUri != null)
                startActivity(new Intent(Intent.ACTION_VIEW, mUri));
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
                
        //case R.id.syntax_highlight:
            //highlightSyntax(0, mEdit.getText().length());
            //break;
            
        case R.id.action_le_windows:
            mLineEnding = LE_WINDOWS;
            updateStatusLineEnding();
            mUndoRedoHelper.markSaved(false);
            break;
                
        case R.id.action_le_unix:
            mLineEnding = LE_UNIX;
            updateStatusLineEnding();
            mUndoRedoHelper.markSaved(false);
            break;
                
        case R.id.action_le_mac:
            mLineEnding = LE_MAC;
            updateStatusLineEnding();
            mUndoRedoHelper.markSaved(false);
            break;
            
        case R.id.select_all:
            mEdit.selectAll();
            break;
            
        case R.id.selection_upper_case:
            replaceSelectedText(getSelectedText().toString().toUpperCase());
            break;
            
        case R.id.selection_lower_case:
            replaceSelectedText(getSelectedText().toString().toLowerCase());
            break;
            
        default:
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        Uri uri = mUri;
        
        Enable(menu.findItem(R.id.action_revert), uri != null && (!mUndoRedoHelper.isSaved() || (mLastModified != Utils.getLastModified(mUri))));
        Enable(menu.findItem(R.id.action_save), uri != null && !mUndoRedoHelper.isSaved());
        //Enable(menu.findItem(R.id.action_save_as), uri != null);
        Enable(menu.findItem(R.id.action_details), uri != null);
        Enable(menu.findItem(R.id.action_share), uri != null);
        Enable(menu.findItem(R.id.action_open_with), uri != null);
        Enable(menu.findItem(R.id.action_undo), mUndoRedoHelper.getCanUndo());
        Enable(menu.findItem(R.id.action_redo), mUndoRedoHelper.getCanRedo());
        Check(menu.findItem(R.id.action_wrap), mWordWrap);
        switch (mLineEnding)
        {
        case LE_WINDOWS:
            Check(menu.findItem(R.id.action_le_windows), true);
            break;
            
        case LE_UNIX:
            Check(menu.findItem(R.id.action_le_unix), true);
            break;
            
        case LE_MAC:
            Check(menu.findItem(R.id.action_le_mac), true);
            break;
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
    
    @Override //TextWatcher
    public void afterTextChanged(Editable s) { }
    
    @Override //TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    
    @Override //TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        Layout layout = mEdit.getLayout();
        if (layout != null && mBrush != null)
        {
            int lineBegin = layout.getLineForOffset(start);
            int lineEnd = layout.getLineForOffset(start + count);
            
            lineBegin = Math.max(lineBegin - 10, 0);
            lineEnd = Math.min(lineEnd + 10, layout.getLineCount() - 1);
            
            int lineBeginOffset = layout.getLineStart(lineBegin);
            int lineEndOffset = layout.getLineEnd(lineEnd);
            
            highlightSyntax(lineBeginOffset, lineEndOffset);
        }
        
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
        menu.removeItem(android.R.id.selectAll);
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
    
    void updateStatusLineEnding()
    {
        int le = -1;
        switch (mLineEnding)
        {
        case LE_WINDOWS:
            le = R.string.status_le_windows_short;
            break;
            
        case LE_UNIX:
            le = R.string.status_le_unix_short;
            break;
            
        case LE_MAC:
            le = R.string.status_le_mac_short;
            break;
        }

        mStatusLineEnding.setText(getString(le));
    }
    
    void updateStatusBrush()
    {
        if (mBrush != null)
            mStatusBrush.setText(mBrush.getName());
        else
            mStatusBrush.setText("");
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
        Uri uri = mUri;
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
        mUri = uri;

        if (uri != null)
            getActionBar().setSubtitle(uri.getLastPathSegment());
        else
            getActionBar().setSubtitle(null);
        
        invalidateOptionsMenu();
        
        updateShareActionProvider();
    }
    
    void open()
    {
        mLastModified = Utils.getLastModified(mUri);
        if (mUri != null)
            new LoadAsyncTask().execute(mUri);
    }
        
    void save()
    {
        if (mUri != null)
            new SaveAsyncTask(mEncoding.getText().toString()).execute(mUri);
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
    
    void openSaf()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, ACTIVITY_OPEN_FILE);
    }
    
    void highlightSyntax(int start, int end)
    {
        Editable e = mEdit.getText();
        if (mBrush != null)
        {
            remove(e, start, end);
           
            SyntaxHighlighter sh = new SyntaxHighlighter(mBrush);
            Map<Range<Integer>, String> regionList = sh.parse(e, start, end);
            for (Map.Entry<Range<Integer>, String> r : regionList.entrySet())
            {
                int color = 0;
                switch (r.getValue())
                {
                case Brush.PREPROCESSOR:
                    color = 0xFFFF6820;
                    break;
                    
                case Brush.KEYWORD:
                    color = 0xFF00FFFF;
                    break;
                    
                case Brush.STRING:
                case Brush.VALUE:
                //case Brush.LITERAL:
                    color = 0xFFFF00FF;
                    break;
                    
                //case Brush.LABEL:
                case Brush.COLOR1:
                    color = 0xFFFFFF00;
                    break;
                    
                case Brush.VARIABLE:
                    color = 0xFF007F7F;
                    break;
                    
                case Brush.COMMENTS:
                    color = 0xFF00FF00;
                    break;
                }
                Object span = new android.text.style.ForegroundColorSpan(color);
                e.setSpan(span, r.getKey().lowerEndpoint(), r.getKey().upperEndpoint(), android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        else
        {
            remove(e, 0, mEdit.getText().length());
        }
    }
    
    private static void remove(Editable editable, int start, int end)
    {
        android.text.style.ForegroundColorSpan[] spans = editable.getSpans(start, end, android.text.style.ForegroundColorSpan.class);
        for (Object s : spans)
            editable.removeSpan(s);
    }
    
    void toast(String msg)
    {
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
    
    private class LoadAsyncTask extends ProgressDialogAsyncTask<Uri, CharSequence[]>
    {
        private String mFileLineEnding = LE_WINDOWS;
        private String mFileEncoding = "UTF-8";
        
        @Override
        protected CharSequence[] doInBackground(Uri... uris)
        {
            CharSequence[] result = new CharSequence[uris.length];

            for (int i = 0; i < uris.length; i++)
            {
                mDlg.setMessage("Loading " + uris[i].getLastPathSegment());
                try
                {
                    mFileEncoding = Utils.ifNull(Utils.detectEncoding(getContentResolver(), uris[i]), "UTF-8");
                    InputStream is = getContentResolver().openInputStream(uris[i]);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, mFileEncoding)))
                    {
                        StringBuffer sb = new StringBuffer();
                        
                        int c;
                        while ((c = br.read()) != -1)
                        {
                            if (c == '\r')
                            {
                                mFileLineEnding = LE_MAC;
                                br.mark(1);
                                int nc = br.read();
                                if (nc != '\n')
                                    br.reset();
                                else
                                    mFileLineEnding = LE_WINDOWS;
                                c = '\n';
                            }
                            else if (c == '\n')
                            {
                                mFileLineEnding = LE_UNIX;
                            }
                            sb.append((char) c);
                        }

                        result[i] = sb;
                    }
                }
                catch (Exception e)
                {
                    mException = e;
                    e.printStackTrace();
                }
                publishProgress((int) ((i + 1.5f)*100f/uris.length));
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
                toast("Exception: " + mException);
            mBrush = SyntaxHighlighter.getBrush(Utils.getFileExtension(mUri));
            if (result[0] != null)
                mEdit.setText(result[0]);
            mLineEnding = mFileLineEnding;
            updateStatusLineEnding();
            updateStatusBrush();
            mUndoRedoHelper.clearHistory();
            mUndoRedoHelper.markSaved(true);
            mLastModified = Utils.getLastModified(mUri);
            mEncoding.setText(mFileEncoding);
            //highlightSyntax(0, mEdit.getText().length());
            super.onPostExecute(result);
        }
    }
    
    private class SaveAsyncTask extends ProgressDialogAsyncTask<Uri, Void>
    {
        private String mFileEncoding;
        
        SaveAsyncTask(String fileEncoding)
        {
            mFileEncoding = fileEncoding;
        }
        
        @Override
        protected Void doInBackground(Uri... uris)
        {
            for (int i = 0; i < uris.length; i++)
            {
                mDlg.setMessage("Saving " + uris[i].getLastPathSegment());
                try
                {
                    OutputStream os = getContentResolver().openOutputStream(uris[i]);
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, mFileEncoding)))
                    {
                        CharSequence cs = mEdit.getText();
                        
                        int begin = 0;
                        int end = 0;
                        while ((end = indexOf(cs, '\n', begin)) != -1)
                        {
                            CharSequence sub = cs.subSequence(begin, end);
                            bw.append(sub);
                            bw.write(mLineEnding);
                            begin = end + 1;
                        }
                        CharSequence sub = cs.subSequence(begin, cs.length());
                        bw.append(sub);
                    }
                }
                catch (Exception e)
                {
                    mException = e;
                    e.printStackTrace();
                }
                publishProgress((int) ((i + 1.5f)*100f/uris.length));
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
                toast("Exception: " + mException);
            else
                toast("Saved");
            mUndoRedoHelper.markSaved(true);
            mLastModified = Utils.getLastModified(mUri);
            super.onPostExecute(result);
        }
    }
}
