package com.djdenpa.quickcalendar.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Event;
import com.djdenpa.quickcalendar.views.components.QuickColorPicker;
import com.djdenpa.quickcalendar.views.components.QuickDatePicker;
import com.djdenpa.quickcalendar.views.components.QuickDurationPicker;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditCalendarEventDialog extends DialogFragment
implements QuickDurationPicker.SaveDurationHandler {

  @Override
  public void saveDurationMillis(long millis) {
    bSetByDuration.setVisibility(View.VISIBLE);
    qdpEndDate.setVisibility(View.VISIBLE);
    qdpDuration.setVisibility(View.INVISIBLE);

    if (millis <= 0) {
      return;
    }

    Calendar startTime = qdpBeginDate.getValue();
    java.util.Calendar endCal = Calendar.getInstance();
    endCal.setTimeInMillis(startTime.getTime().getTime() + millis);
    qdpEndDate.setValue(endCal);
  }

  public interface EditCalendarNameListener {
    void saveEvent(Event event);
    void finishDialog();
  }

  public static final String BUNDLE_CALENDAR_EVENT = "BUNDLE_CALENDAR_EVENT";

  EditCalendarNameListener mListener;

  boolean mIsLargeLayout;

  View mRootView = null;

  @BindView(R.id.t_edit_event)
  Toolbar t_toolbar;

  Event mEvent;
  @BindView(R.id.et_event_name)
  EditText etEventName;

  @BindView(R.id.qdp_begin_date)
  QuickDatePicker qdpBeginDate;
  @BindView(R.id.qdp_end_date)
  QuickDatePicker qdpEndDate;

  @BindView(R.id.qdp_duration)
  QuickDurationPicker qdpDuration;

  @BindView(R.id.qcp_color)
  QuickColorPicker qcpColor;

  @BindView(R.id.b_set_by_duration)
  Button bSetByDuration;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

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

  private View inflateDialogView(LayoutInflater inflater){
    View view = inflater.inflate( R.layout.dialog_edit_calendar_event, null, false );

    ButterKnife.bind(this, view);

    etEventName.setText(mEvent.name);

    // on first create, default on the event name
    if (mEvent.id <= 0) {
      etEventName.requestFocus();
    }

    java.util.Calendar beginCal = Calendar.getInstance();
    beginCal.setTimeInMillis(mEvent.eventStartUTC);
    qdpBeginDate.setValue(beginCal);
    java.util.Calendar endCal = Calendar.getInstance();
    endCal.setTimeInMillis(mEvent.eventStartUTC + mEvent.eventDurationMs);
    qdpEndDate.setValue(endCal);

    qdpDuration.setCallback(this);

    qcpColor.setColor(mEvent.color);

    bSetByDuration.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        //hide so we show the duration window
        bSetByDuration.setVisibility(View.INVISIBLE);
        qdpEndDate.setVisibility(View.INVISIBLE);
        qdpDuration.setVisibility(View.VISIBLE);

        qdpDuration.init();

        long startTime = qdpBeginDate.getValue().getTime().getTime();
        long endTime = qdpEndDate.getValue().getTime().getTime();
        long duration = endTime - startTime;
        qdpDuration.setDurationMillis(duration);

        qdpDuration.focus();

      }
    });

    t_toolbar.setVisibility(View.VISIBLE);
    t_toolbar.inflateMenu(R.menu.menu_event_edit);
    t_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
          saveEvent();
          Dialog dialog = getDialog();
          if (dialog != null) {
            dialog.dismiss();
          } else {
            getFragmentManager().popBackStack();
          }
          return true;
        }
        return true;
      }
    });
    t_toolbar.setTitle(getDialogTitleResId());

    return view;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mListener.finishDialog();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (mRootView == null) {
      mRootView = inflateDialogView(inflater);
    }
    return mRootView;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    if (mIsLargeLayout) {
      LayoutInflater inflater = LayoutInflater.from(getActivity());
      if (mRootView == null) {
        mRootView = inflateDialogView(inflater);
      }
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setView(mRootView);
      Dialog dialog = builder.create();
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    } else {
      return super.onCreateDialog(savedInstanceState);
    }
  }

  private int getDialogTitleResId(){
    return (mEvent.id > 0 ?
            (R.string.edit_event_title) :
            (R.string.add_event_title));
  }

  private void saveEvent(){
    long startTime = qdpBeginDate.getValue().getTime().getTime();
    long endTime = qdpEndDate.getValue().getTime().getTime();
    long duration = endTime - startTime;
    if (duration < 1000) {
      duration = 1000;
    }
    // trim so no less than second
    duration = duration / 1000 * 1000;
    mEvent.name = etEventName.getText().toString();
    mEvent.eventStartUTC = startTime;
    mEvent.eventDurationMs = duration;
    mEvent.color = qcpColor.getColor();
    mListener.saveEvent(mEvent);
  }
}
