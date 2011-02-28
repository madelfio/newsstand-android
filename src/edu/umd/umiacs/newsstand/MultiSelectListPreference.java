package edu.umd.umiacs.newsstand;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class MultiSelectListPreference extends ListPreference {
    
    int mLimit = 3;
    ListView mList = null;
    String mSelection = "";

    public MultiSelectListPreference(Context context) {
        this(context, null);
    }

    public MultiSelectListPreference(Context context, AttributeSet attr) {
        super(context, attr);
    } 

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        //mList.setSelection(mSelection);
    } 

    @Override
    protected View onCreateDialogView() {
        //mList = new ListView(getContext(), this.getEntries(), mLimit);
        return(mList);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            //if (callChangeListener(mList.getSelection())) {
            //    mSelection = mList.getSelection();
            //    persistString(mSelection);
            //}
        }
    } 

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        mSelection =(restoreValue ? getPersistedString(mSelection) : (String)"");
    }
}
