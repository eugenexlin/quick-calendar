package com.djdenpa.quickcalendar.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.djdenpa.quickcalendar.R;

public class GenericSingleSelectListDialog extends DialogFragment {

  public interface GenericSpinnerDialogListener {
    void handleSpinnerDialog(int code, String value);
  }
  Context mContext;
  GenericSpinnerDialogListener mListener;

  public static final String BUNDLE_STRING_CURRENT_VALUE = "BUNDLE_STRING_CURRENT_VALUE";
  public static final String BUNDLE_INT_STRING_TITLE = "BUNDLE_INT_STRING_TITLE";
  public static final String BUNDLE_INT_ARRAY_ID = "BUNDLE_INT_ARRAY_ID";
  private String mCurrentValue;
  private int mTitleResource;
  private int mArrayResource;
  private Spinner mSpinner;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCurrentValue = getArguments().getString(BUNDLE_STRING_CURRENT_VALUE);
    mTitleResource = getArguments().getInt(BUNDLE_INT_STRING_TITLE);
    if (!getArguments().containsKey(BUNDLE_INT_ARRAY_ID)){
      throw new IllegalArgumentException("Missing array resource id into key BUNDLE_INT_ARRAY_ID");
    }
    mArrayResource = getArguments().getInt(BUNDLE_INT_ARRAY_ID);

    mContext = getContext();

    try {
      mListener = (GenericSpinnerDialogListener) getTargetFragment();
    } catch (ClassCastException e) {
      throw new ClassCastException("Parent must implement GenericSpinnerDialogListener");
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    String title = getResources().getString(mTitleResource) + " (current: " + mCurrentValue + ")";
    builder.setTitle(title)
            .setItems(mArrayResource, (dialog, which) -> {
              String value = getResources().getStringArray(mArrayResource)[which];
              mListener.handleSpinnerDialog(getTargetRequestCode(), value);
            });
    return builder.create();
  }
}
