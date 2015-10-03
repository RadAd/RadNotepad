package au.radsoft;

import android.os.AsyncTask;
import android.app.ProgressDialog;

abstract class ProgressDialogAsyncTask<Params, Result> extends AsyncTask<Params, Integer, Result>
{
    protected ProgressDialog mDlg;
    protected Throwable mException;
    
    @Override
    protected void onPreExecute()
    {
        if (mDlg != null && !mDlg.isShowing())
            mDlg.show();
    }
    
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if (mDlg != null)
        {
            if (values.length > 0)
                mDlg.setProgress(values[0]);
            if (values.length > 1)
                mDlg.setSecondaryProgress(values[1]);
        }
    }

    @Override
    protected void onPostExecute(Result result)
    {
        if (mDlg != null)
            mDlg.dismiss();
    }
}
