package au.radsoft;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
//import android.util.Log;

public class Preferences extends android.preference.PreferenceActivity
{
    //private static final String TAG = Preferences.class.getSimpleName();
    
    public static final int PREF_FONT_SIZE_DEFAULT = 10;
    public static final String PREF_FONT_SIZE = "pref_font_size";
    
    static void show(Context context)
    {
        android.content.Intent myIntent = new android.content.Intent(context, Preferences.class);
        context.startActivity(myIntent);
    }
    
    static SharedPreferences init(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        return sharedPref;
    }
    
    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
    }
}
