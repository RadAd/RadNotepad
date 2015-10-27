package au.radsoft;

import android.graphics.Rect;
import android.text.TextUtils;
import android.text.method.TransformationMethod;
import android.text.method.ReplacementTransformationMethod;
import android.view.View;

class ShowSpacesTransformationMethod extends ReplacementTransformationMethod
{
    // alternatives
    // \u2420 // Symbol for space
    // \u2423 // OpenBox
    // \u00B7 // Middle dot
    static final char[] original = { ' ', '\t' };
    static final char[] replacement = { 'a', 'b' };
    
    @Override
    protected char[] getOriginal()
    {
        return original;
    }
    
    @Override
    protected char[] getReplacement()
    {
        return replacement;
    }
}
