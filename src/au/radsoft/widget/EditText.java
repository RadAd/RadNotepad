package au.radsoft.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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
    
    // Fix a bug in TextView that isn't correctly capturing TAB key events
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_TAB) {
            int which = doKeyDown(keyCode, event, null);
            if (which == 0) {
                return super.onKeyDown(keyCode, event);
            }

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
    // Fix a bug in TextView that isn't correctly capturing TAB key events
    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_TAB) {
            KeyEvent down = KeyEvent.changeAction(event, KeyEvent.ACTION_DOWN);

            int which = doKeyDown(keyCode, down, event);
            if (which == 0) {
                // Go through default dispatching.
                return super.onKeyMultiple(keyCode, repeatCount, event);
            }
            if (which == -1) {
                // Consumed the whole thing.
                return true;
            }

            repeatCount--;
            
            // Some missing variables
            android.text.method.KeyListener mKeyListener = getKeyListener();
            android.text.method.MovementMethod mMovement = getMovementMethod();
            android.text.Editable mText = getEditableText();

            // We are going to dispatch the remaining events to either the input
            // or movement method.  To do this, we will just send a repeated stream
            // of down and up events until we have done the complete repeatCount.
            // It would be nice if those interfaces had an onKeyMultiple() method,
            // but adding that is a more complicated change.
            KeyEvent up = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
            if (which == 1) {
                // mEditor and mEditor.mInput are not null from doKeyDown
                mKeyListener.onKeyUp(this, /*(Editable)*/ mText, keyCode, up);
                while (--repeatCount > 0) {
                    mKeyListener.onKeyDown(this, /*(Editable)*/ mText, keyCode, down);
                    mKeyListener.onKeyUp(this, /*(Editable)*/ mText, keyCode, up);
                }
                //hideErrorIfUnchanged();

            } else if (which == 2) {
                // mMovement is not null from doKeyDown
                mMovement.onKeyUp(this, /*(Spannable)*/ mText, keyCode, up);
                while (--repeatCount > 0) {
                    mMovement.onKeyDown(this, /*(Spannable)*/ mText, keyCode, down);
                    mMovement.onKeyUp(this, /*(Spannable)*/ mText, keyCode, up);
                }
            }

            return true;
        } else {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
    }

    // Taken from TextView.java to fix bug in shouldAdvanceFocusOnTab()
    private int doKeyDown(int keyCode, KeyEvent event, KeyEvent otherEvent) {
        if (!isEnabled()) {
            return 0;
        }
        
        // Some missing variables
        boolean mPreventDefaultMovement = false;
        android.text.method.KeyListener mKeyListener = getKeyListener();
        android.text.Editable mText = getEditableText();
        Layout mLayout = getLayout();
        android.text.method.MovementMethod mMovement = getMovementMethod();

        // If this is the initial keydown, we don't want to prevent a movement away from this view.
        // While this shouldn't be necessary because any time we're preventing default movement we
        // should be restricting the focus to remain within this view, thus we'll also receive
        // the key up event, occasionally key up events will get dropped and we don't want to
        // prevent the user from traversing out of this on the next key down.
        if (event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(keyCode)) {
            mPreventDefaultMovement = false;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB:
                if (event.hasNoModifiers() || event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                    if (shouldAdvanceFocusOnTab()) {
                        return 0;
                    }
                }
                break;
        }

        if (mKeyListener != null) {
            boolean doDown = true;
            if (otherEvent != null) {
                try {
                    beginBatchEdit();
                    final boolean handled = mKeyListener.onKeyOther(this, /*(Editable)*/ mText,
                            otherEvent);
                    //hideErrorIfUnchanged();
                    doDown = false;
                    if (handled) {
                        return -1;
                    }
                } catch (AbstractMethodError e) {
                    // onKeyOther was added after 1.0, so if it isn't
                    // implemented we need to try to dispatch as a regular down.
                } finally {
                    endBatchEdit();
                }
            }

            if (doDown) {
                beginBatchEdit();
                final boolean handled = mKeyListener.onKeyDown(this, /*(Editable)*/ mText,
                        keyCode, event);
                endBatchEdit();
                //hideErrorIfUnchanged();
                if (handled) return 1;
            }
        }

        // bug 650865: sometimes we get a key event before a layout.
        // don't try to move around if we don't know the layout.

        if (mMovement != null && mLayout != null) {
            boolean doDown = true;
            if (otherEvent != null) {
                try {
                    boolean handled = mMovement.onKeyOther(this, /*(Editable)*/ mText,
                            otherEvent);
                    doDown = false;
                    if (handled) {
                        return -1;
                    }
                } catch (AbstractMethodError e) {
                    // onKeyOther was added after 1.0, so if it isn't
                    // implemented we need to try to dispatch as a regular down.
                }
            }
            if (doDown) {
                if (mMovement.onKeyDown(this, /*(Spannable)*/ mText, keyCode, event)) {
                    if (event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(keyCode)) {
                        mPreventDefaultMovement = true;
                    }
                    return 2;
                }
            }
        }

        return mPreventDefaultMovement && !KeyEvent.isModifierKey(keyCode) ? -1 : 0;
    }
    
    // Taken from TextView.java to fix bug in shouldAdvanceFocusOnTab()
    // See https://code.google.com/p/android/issues/detail?id=189827
    private boolean shouldAdvanceFocusOnTab() {
        int mInputType = getInputType();
        boolean mSingleLine = false; // Assume false
        
        if (getKeyListener() != null && !mSingleLine &&
                (mInputType & EditorInfo.TYPE_MASK_CLASS) == EditorInfo.TYPE_CLASS_TEXT) {
            if ((mInputType & EditorInfo.TYPE_TEXT_FLAG_IME_MULTI_LINE) != 0
                    || (mInputType & EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0) {
                return false;
            }
        }
        return true;
    }
}
