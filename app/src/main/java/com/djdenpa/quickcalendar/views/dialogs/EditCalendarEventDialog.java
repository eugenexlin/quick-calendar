package com.djdenpa.quickcalendar.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Event;

import butterknife.BindView;

public class EditCalendarEventDialog extends DialogFragment {
  public interface EditCalendarNameListener {
    void saveEvent(Event event);
  }

  public static final String BUNDLE_CALENDAR_EVENT = "BUNDLE_CALENDAR_EVENT";

  EditCalendarNameListener mListener;

  Event mEvent;
  @BindView(R.id.et_event_name)
  EditText etEventName;
  @BindView(R.id.et_event_start)
  TextView etEventStart;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mEvent = getArguments().getParcelable(BUNDLE_CALENDAR_EVENT);
    if (mEvent == null) {
      mEvent = new Event();
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
    final View rootView = inflater.inflate(R.layout.dialog_edit_calendar_event, null);

    etEventName.setText(mEvent.name);
    etEventName.setText((int) mEvent.eventStartUTC);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setView(rootView)
            .setTitle(getString(R.string.edit_calendar_name_title))
            .setPositiveButton(R.string.save, (dialog, id) -> {
              mListener.saveEvent(mEvent);
            })
            .setNegativeButton(R.string.cancel, (dialog, id) -> {
            });
    return builder.create();
  }

}
