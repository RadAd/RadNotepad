package au.radsoft;

// From https://gist.github.com/kidinov/6900164#file-undo_redo-android

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
//import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UndoRedoHelper {
    private static final String TAG = UndoRedoHelper.class.getCanonicalName();

    public interface HistoryChangedListener {
        public void onHistoryChanged(UndoRedoHelper helper);
    }

    private boolean mIsUndoOrRedo = false;
    private EditHistory mEditHistory = new EditHistory();
    private EditTextChangeListener mChangeListener = new EditTextChangeListener();
    private TextView mTextView;
    private List<HistoryChangedListener> listeners = new ArrayList<HistoryChangedListener>();

    public UndoRedoHelper(TextView textView) {
        mTextView = textView;
        mTextView.addTextChangedListener(mChangeListener);
        markSaved(true);
    }

    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(TAG + ".history", mEditHistory);
    }

    public void onRestoreInstanceState(Bundle state) {
        mEditHistory = Utils.ifNull((EditHistory) state.getParcelable(TAG + ".history"), mEditHistory);
    }

    public void addHistoryChangedListener(HistoryChangedListener o) {
        listeners.add(o);
    }

    private void onHistoryChanged() {
        if (listeners != null)
        {
            for (HistoryChangedListener l : listeners)
                l.onHistoryChanged(this);
        }
    }

    public void disconnect() {
        mTextView.removeTextChangedListener(mChangeListener);
    }

    public void setMaxHistorySize(int maxHistorySize) {
        mEditHistory.setMaxHistorySize(maxHistorySize);
    }

    public void clearHistory() {
        mEditHistory.clear();
        onHistoryChanged();
    }

    public void markSaved(boolean saved) {
        if (saved)
            mEditHistory.mSaved = mEditHistory.getCurrent();
        else
            mEditHistory.mSaved = new EditItem(0, null, null);
        onHistoryChanged();
    }

    public boolean isSaved() {
        return mEditHistory.mSaved == mEditHistory.getCurrent();
    }

    public boolean getCanUndo() {
        return (mEditHistory.mmPosition > 0);
    }

    public void undo() {
        EditItem edit = mEditHistory.getPrevious();
        if (edit == null) {
            return;
        }

        Editable text = mTextView.getEditableText();
        int start = edit.mmStart;
        int end = start + (edit.mmAfter != null ? edit.mmAfter.length() : 0);
        mIsUndoOrRedo = true;
        text.replace(start, end, edit.mmBefore);
        mIsUndoOrRedo = false;
        for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
            text.removeSpan(o);
        }
        Selection.setSelection(text, edit.mmBefore == null ? start : (start + edit.mmBefore.length()));
        onHistoryChanged();
    }

    public boolean getCanRedo() {
        return (mEditHistory.mmPosition < mEditHistory.mmHistory.size());
    }

    public void redo() {
        EditItem edit = mEditHistory.getNext();
        if (edit == null) {
            return;
        }
        Editable text = mTextView.getEditableText();
        int start = edit.mmStart;
        int end = start + (edit.mmBefore != null ? edit.mmBefore.length() : 0);
        mIsUndoOrRedo = true;
        text.replace(start, end, edit.mmAfter);
        mIsUndoOrRedo = false;
        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.
        for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
            text.removeSpan(o);
        }
        Selection.setSelection(text, edit.mmAfter == null ? start
                : (start + edit.mmAfter.length()));
        onHistoryChanged();
    }

    // =================================================================== //

    private final static class EditHistory implements Parcelable {
        public static final Parcelable.Creator<EditHistory> CREATOR
            = new Parcelable.Creator<EditHistory>() {
                public EditHistory createFromParcel(Parcel in) {
                    return new EditHistory(in);
                }
                public EditHistory[] newArray(int size) {
                    return new EditHistory[size];
                }
            };

        private int mmPosition = 0;
        private int mmMaxHistorySize = -1;
        private EditItem mSaved = new EditItem(0, null, null);
        private final LinkedList<EditItem> mmHistory = new LinkedList<EditItem>();

        private EditHistory() {
        }

        private EditHistory(Parcel in) {
            mmPosition = in.readInt();
            mmMaxHistorySize = in.readInt();
            in.readList(mmHistory, null);
            int saved = in.readInt();
            if (saved >= 0)
                mSaved = mmHistory.get(saved);
        }

        @Override   // From Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mmPosition);
            dest.writeInt(mmMaxHistorySize);
            dest.writeList(mmHistory);
            dest.writeInt(mmHistory.indexOf(mSaved));
        }

        @Override   // From Parcelable
        public int describeContents() {
            return 0;
        }

        private void clear() {
            mmPosition = 0;
            mmHistory.clear();
        }

        private void add(EditItem item) {
            while (mmHistory.size() > mmPosition) {
                mmHistory.removeLast();
            }
            mmHistory.add(item);
            mmPosition++;
            if (mmMaxHistorySize >= 0) {
                trimHistory();
            }
        }

        private void setMaxHistorySize(int maxHistorySize) {
            mmMaxHistorySize = maxHistorySize;
            if (mmMaxHistorySize >= 0) {
                trimHistory();
            }
        }

        private void trimHistory() {
            while (mmHistory.size() > mmMaxHistorySize) {
                mmHistory.removeFirst();
                mmPosition--;
            }
            if (mmPosition < 0) {
                mmPosition = 0;
            }
        }

        private EditItem getCurrent() {
            if (mmPosition == 0) {
                return null;
            }
            return mmHistory.get(mmPosition - 1);
        }

        private EditItem getPrevious() {
            if (mmPosition == 0) {
                return null;
            }
            mmPosition--;
            return mmHistory.get(mmPosition);
        }

        private EditItem getNext() {
            if (mmPosition >= mmHistory.size()) {
                return null;
            }
            EditItem item = mmHistory.get(mmPosition);
            mmPosition++;
            return item;
        }
    }

    private final static class EditItem implements Parcelable {
        public static final Parcelable.Creator<EditItem> CREATOR
            = new Parcelable.Creator<EditItem>() {
                public EditItem createFromParcel(Parcel in) {
                    return new EditItem(in);
                }
                public EditItem[] newArray(int size) {
                    return new EditItem[size];
                }
            };

        private int mmStart;
        private CharSequence mmBefore;
        private CharSequence mmAfter;

        public EditItem(int start, CharSequence before, CharSequence after) {
            mmStart = start;
            mmBefore = before;
            mmAfter = after;
        }

        private EditItem(Parcel in) {
            mmStart = in.readInt();
            mmBefore = (CharSequence) in.readValue(null);
            mmAfter = (CharSequence) in.readValue(null);
        }

        @Override   // From Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mmStart);
            dest.writeValue(mmBefore);
            dest.writeValue(mmAfter);
        }

        @Override   // From Parcelable
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            return "EditItem{" +
                    "mmStart=" + mmStart +
                    ", mmBefore=" + mmBefore +
                    ", mmAfter=" + mmAfter +
                    '}';
        }
    }

    enum ActionType {
        INSERT, DELETE, PASTE, NOT_DEF;
    }

    private final class EditTextChangeListener implements TextWatcher {
        private CharSequence mBeforeChange;
        private CharSequence mAfterChange;
        private ActionType lastActionType = ActionType.NOT_DEF;
        private long lastActionTime = 0;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mIsUndoOrRedo) {
                return;
            }

            mBeforeChange = s.subSequence(start, start + count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mIsUndoOrRedo) {
                return;
            }

            mAfterChange = s.subSequence(start, start + count);
            makeBatch(start);
            onHistoryChanged();
        }

        private void makeBatch(int start) {
            ActionType at = getActionType();
            EditItem editItem = mEditHistory.getCurrent();
            // TODO keep spaces separate from characters
            if (editItem == null || lastActionType != at || !canJoin(at, editItem, start) || (System.currentTimeMillis() - lastActionTime) > 500) {
                mEditHistory.add(new EditItem(start, mBeforeChange, mAfterChange));
            } else {
                switch (at) {
                case DELETE:
                    editItem.mmStart = start;
                    editItem.mmBefore = TextUtils.concat(mBeforeChange, editItem.mmBefore);
                    break;

                case INSERT:
                    if (TextUtils.isEmpty(mBeforeChange))
                        editItem.mmAfter = TextUtils.concat(editItem.mmAfter, mAfterChange);
                    else
                        editItem.mmAfter = mAfterChange;
                    break;
                }
            }
            lastActionType = at;
            lastActionTime = System.currentTimeMillis();
        }

        private boolean canJoin(ActionType at, EditItem editItem, int start) {
            switch (at) {
            case DELETE:
                return (start + mBeforeChange.length()) == editItem.mmStart;

            case INSERT:
                if (TextUtils.isEmpty(mBeforeChange))
                    return start == (editItem.mmStart + editItem.mmAfter.length());
                else
                    return start == editItem.mmStart;

            default:
            case PASTE:
                return false;
            }
        }

        private ActionType getActionType() {
            if (!TextUtils.isEmpty(mBeforeChange) && TextUtils.isEmpty(mAfterChange)) {
                return ActionType.DELETE;
            } else if (TextUtils.isEmpty(mBeforeChange) && !TextUtils.isEmpty(mAfterChange)) {
                return ActionType.INSERT;
            } else if (mBeforeChange.length() <= mAfterChange.length() && TextUtils.regionMatches(mBeforeChange, 0, mAfterChange, 0, mBeforeChange.length())) {
                return ActionType.INSERT;
            } else {
                return ActionType.PASTE;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
