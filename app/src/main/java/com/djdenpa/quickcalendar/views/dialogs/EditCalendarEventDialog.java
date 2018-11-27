package com.djdenpa.quickcalendar.views.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Event;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditCalendarEventDialog extends DialogFragment {
  public interface EditCalendarNameListener {
    void saveEvent(Event event);
  }

  public static final String BUNDLE_CALENDAR_EVENT = "BUNDLE_CALENDAR_EVENT";

  EditCalendarNameListener mListener;

  Event mEvent;
  @BindView(R.id.et_event_name)
  EditText etEventName;
  @BindView(R.id.et_event_start_year)
  TextView etStartYear;
  @BindView(R.id.et_event_start_month)
  TextView etStartMonth;
  @BindView(R.id.et_event_start_day)
  TextView etStartDay;

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
    @SuppressLint("InflateParams") final View rootView = inflater.inflate( R.layout.dialog_edit_calendar_event, null, false );

    ButterKnife.bind(this, rootView);

    etEventName.setText(mEvent.name);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setView(rootView)
            .setTitle(mEvent.id > 0 ?
                    (R.string.edit_event_title) :
                    (R.string.add_event_title)
            )
            .setPositiveButton(R.string.save, (dialog, id) -> {
              mListener.saveEvent(mEvent);
            })
            .setNegativeButton(R.string.cancel, (dialog, id) -> {
            });
    Dialog dialog = builder.create();
    // taken from stack overflow
//    // questions/49202071/make-dialog-take-as-much-place-as-possible-with-maximum-dimensions
//    dialog.setOnShowListener(dialogInterface -> {
//      Rect fgPadding = new Rect();
//      dialog.getWindow().getDecorView().getBackground().getPadding(fgPadding);
//      DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
//      int height = metrics.heightPixels - fgPadding.top - fgPadding.bottom;
//      int width = metrics.widthPixels - fgPadding.top - fgPadding.bottom;
//      rootView.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
//    });
    return dialog;
  }

}
