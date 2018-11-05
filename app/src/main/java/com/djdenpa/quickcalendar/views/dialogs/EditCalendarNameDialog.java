package com.djdenpa.quickcalendar.views.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.djdenpa.quickcalendar.R;

public class EditCalendarNameDialog extends DialogFragment {

  public interface EditCalendarNameListener {
    void setCalendarName(String name);
  }

  public static final String BUNDLE_CALENDAR_NAME = "BUNDLE_CALENDAR_NAME";

  EditCalendarNameListener mListener;

  String mCalenderName;
  EditText mCalendarNameEditText;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCalenderName = getArguments().getString(BUNDLE_CALENDAR_NAME);
    if (mCalenderName == null) {
      mCalenderName = "";
    }

    try {
      mListener = (EditCalendarNameListener) getTargetFragment();
    } catch (ClassCastException e) {
      throw new ClassCastException("Fragment must implement EditCalendarNameListener");
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    final View rootView = inflater.inflate(R.layout.dialog_edit_calendar_name, null);

    mCalendarNameEditText = rootView.findViewById(R.id.et_calendar_name);
    mCalendarNameEditText.setText(mCalenderName);
    mCalendarNameEditText.selectAll();

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setView(rootView)
            .setTitle(getString(R.string.edit_calendar_name_title))
            .setPositiveButton(R.string.save, (dialog, id) -> {
              mListener.setCalendarName(mCalendarNameEditText.getText().toString());
            })
            .setNegativeButton(R.string.cancel, (dialog, id) -> {
            });
    return builder.create();
  }

}
