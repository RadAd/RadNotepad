package au.radsoft.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import au.radsoft.R;

public class EditIntegerPreference extends EditTextPreference
{
    private NumberPicker picker;
    private int min = 0;
    private int max = Integer.MAX_VALUE;
    
    public EditIntegerPreference(Context context) {
        super(context);
        init(null);
    }

    public EditIntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EditIntegerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
        setDialogLayoutResource(R.layout.integer_preference);
        if (attrs != null) {
            min = attrs.getAttributeIntValue(null, "min", min);
            max = attrs.getAttributeIntValue(null, "max", max);
        }
    }

    @Override
    public String getText() {
        return String.valueOf(getValue());
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if (getSharedPreferences().contains(getKey()))
            return String.valueOf(getValue());
        else
            return super.getPersistedString(defaultReturnValue);
    }

    @Override
    public void setText(String text) {
        getSharedPreferences().edit().putInt(getKey(), Integer.parseInt(text)).commit();
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        picker = (NumberPicker) view.findViewById(R.id.value);
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(getValue());
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            persistInt(picker.getValue());
            picker = null;
        }
    }
    
    int getValue() {
        return getSharedPreferences().getInt(getKey(), 0);
    }
}
