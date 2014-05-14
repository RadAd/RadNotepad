package au.radsoft;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.net.*;
import java.io.*;
import android.text.*;

public class MainActivity extends Activity
{
	static final int ACTIVITY_OPEN_FILE = 1;
    static final int ACTIVITY_SAVE_FILE = 2;
    
    static final String LE_WINDOWS = "\r\n";
    static final String LE_UNIX = "\n";
    static final String LE_MAC = "\r";
	
	EditText mEdit;
	UndoRedoHelper mUndoRedoHelper;
    ShareActionProvider myShareActionProvider;
    boolean mWordWrap = false;
    String mLineEnding = LE_WINDOWS;
    long mLastModified = -1;
	
	Uri mUri;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mEdit = (EditText) findViewById(R.id.edit);
		mEdit.setHorizontallyScrolling(!mWordWrap); // bug when set in xml
		mUndoRedoHelper = new UndoRedoHelper(mEdit);
        mEdit.addTextChangedListener(new TextWatcher()
            {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void afterTextChanged(Editable s) {}
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    invalidateOptionsMenu();
                }
            }
        );
        
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
    public void onResume()
	{
		super.onResume();
        
        if (mLastModified != getLastModified(mUri))
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
        if (!mUndoRedoHelper.isSaved())
        {
            new AlertDialog.Builder(this)
                .setTitle("Save?")
                .setMessage("Do you wish to save before exiting?")
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            MainActivity.super.onBackPressed();
                        }
                    })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            save();
                            MainActivity.super.onBackPressed();
                        }
                    })
                .create().show();
        }
        else
            super.onBackPressed();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        
        getMenuInflater().inflate(R.menu.menu, menu);
        getMenuInflater().inflate(R.menu.line_ending, menu.addSubMenu(R.string.action_line_ending));
        
        MenuItem item = menu.findItem(R.id.action_share);
        myShareActionProvider = (ShareActionProvider) item.getActionProvider();
        updateShareActionProvider();
        
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_open_content:
            openChooser();
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
                msg.append("MIME: " + ifNull(getMimeType(uri), ""));
                
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
				
		case R.id.action_le_windows:
            mLineEnding = LE_WINDOWS;
            mUndoRedoHelper.markSaved(false);
			break;
				
		case R.id.action_le_unix:
            mLineEnding = LE_UNIX;
            mUndoRedoHelper.markSaved(false);
			break;
				
		case R.id.action_le_mac:
            mLineEnding = LE_MAC;
            mUndoRedoHelper.markSaved(false);
			break;
		}
		
		return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
		
		Uri uri = mUri;
        
        Enable(menu.findItem(R.id.action_revert), uri != null); // TODO When detect save changed -- && !mUndoRedoHelper.isSaved()
        Enable(menu.findItem(R.id.action_save), uri != null && !mUndoRedoHelper.isSaved());
        //Enable(menu.findItem(R.id.action_save_as), uri != null);
        Enable(menu.findItem(R.id.action_details), uri != null);
        Enable(menu.findItem(R.id.action_share), uri != null);
        Enable(menu.findItem(R.id.action_open_with), uri != null);
        Enable(menu.findItem(R.id.action_undo), mUndoRedoHelper.getCanUndo());
        Enable(menu.findItem(R.id.action_redo), mUndoRedoHelper.getCanRedo());
        menu.findItem(R.id.action_wrap).setChecked(mWordWrap);
        switch (mLineEnding)
        {
        case LE_WINDOWS:
            menu.findItem(R.id.action_le_windows).setChecked(true);
            break;
            
        case LE_UNIX:
            menu.findItem(R.id.action_le_unix).setChecked(true);
            break;
            
        case LE_MAC:
            menu.findItem(R.id.action_le_mac).setChecked(true);
            break;
        }
		
		return true;
    }
    
    static void Enable(MenuItem mi, boolean enable)
    {
        mi.setEnabled(enable);
        android.graphics.drawable.Drawable icon = mi.getIcon();
        if (icon != null)
            icon.setAlpha(enable ? 255 : 130);
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
                intent.setType(ifNull(getMimeType(uri), "*/*"));
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
        mLastModified = getLastModified(mUri);
        if (mUri != null)
            new LoadAsyncTask().execute(mUri);
	}
		
	void save()
	{
        if (mUri != null)
            new SaveAsyncTask().execute(mUri);
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
	
	void toast(String msg)
	{
		Toast toast = Toast.makeText(this, msg,Toast.LENGTH_LONG);
		toast.show();
	}
    
    private class LoadAsyncTask extends ProgressDialogAsyncTask<Uri, CharSequence[]>
    {
        private String mFileLineEnding = LE_WINDOWS;
        
        @Override
        protected CharSequence[] doInBackground(Uri... uris)
        {
            CharSequence[] result = new CharSequence[uris.length];

            for (int i = 0; i < uris.length; i++)
            {
                mDlg.setMessage("Loading " + uris[i].getLastPathSegment());
                try
                {
                    InputStream is = getContentResolver().openInputStream(uris[i]);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is)))
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
            if (result[0] != null)
                mEdit.setText(result[0]);
            mLineEnding = mFileLineEnding;
            mUndoRedoHelper.clearHistory();
            mUndoRedoHelper.markSaved(true);
            mLastModified = getLastModified(mUri);
            super.onPostExecute(result);
        }
    }
    
    private class SaveAsyncTask extends ProgressDialogAsyncTask<Uri, Void>
    {
        @Override
        protected Void doInBackground(Uri... uris)
        {
            for (int i = 0; i < uris.length; i++)
            {
                mDlg.setMessage("Saving " + uris[i].getLastPathSegment());
                try
                {
                    OutputStream os = getContentResolver().openOutputStream(uris[i]);
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os)))
                    {
                        CharSequence cs = mEdit.getText();
                        
                        int begin = 0;
                        int end = 0;
                        while ((end = find(cs, begin, '\n')) != -1)
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
            mLastModified = getLastModified(mUri);
            super.onPostExecute(result);
        }
    }
    
    private static String getMimeType(Uri uri)
    {
        android.webkit.MimeTypeMap mtm = android.webkit.MimeTypeMap.getSingleton();
        String extension = mtm.getFileExtensionFromUrl(uri.toString());
        return mtm.getMimeTypeFromExtension(extension);
    }
    
    private static String ifNull(String input, String ifnull)
    {
        return input == null ? ifnull : input;
    }
    
    private static long getLastModified(Uri uri)
    {
        if (uri != null && uri.getScheme().equals("file"))
        {
            File f = new File(uri.getPath());
            return f.lastModified();
        }
        else
            return -1;
    }
    
    private static int find(CharSequence cs, int o, char c)
    {
        while (o < cs.length())
        {
            if (cs.charAt(o) == c)
                return o;
            ++o;
        }
        return -1;
    }
}
