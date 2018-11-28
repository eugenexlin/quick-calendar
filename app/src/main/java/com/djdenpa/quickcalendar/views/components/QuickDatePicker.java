package com.djdenpa.quickcalendar.views.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.djdenpa.quickcalendar.R;

import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuickDatePicker extends FrameLayout {
  Context mContext;

  @BindView(R.id.et_year)
  EditText etYear;
  @BindView(R.id.et_month)
  EditText etMonth;
  @BindView(R.id.et_day)
  EditText etDay;
  @BindView(R.id.et_hour)
  EditText etHour;
  @BindView(R.id.et_minute)
  EditText etMinute;
  @BindView(R.id.s_am_pm)
  Spinner sAmPm;

  public QuickDatePicker(Context context) {
    super(context);
    mContext = context;
    onCreate();
  }

  public QuickDatePicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    onCreate();
  }

  public QuickDatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
    onCreate();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public QuickDatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mContext = context;
    onCreate();
  }


  public void onCreate(){

    LayoutInflater inflater = LayoutInflater.from(mContext);
    final View rootView = inflater.inflate( R.layout.view_quick_date_picker, null, false );
    addView(rootView);

    ButterKnife.bind(this, rootView);

    setTextPostChangeOverflow(etYear, etMonth, 4);
    setTextPostChangeOverflow(etMonth, etDay, 2);
    setTextPostChangeOverflow(etDay, etHour, 2);
    setTextPostChangeOverflow(etHour, etMinute, 2);

    String[] arr = mContext.getResources().getStringArray(R.array.am_pm);
    sAmPm.setAdapter(new ArrayAdapter<>(mContext, R.layout.view_spinner_item, arr ));
  }

  private void setTextPostChangeOverflow(EditText targetEditText, EditText nextEditText, int maxLength){
    targetEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
      @Override
      public void afterTextChanged(Editable s) {
        if (targetEditText.getText().length() > maxLength) {
          String str = targetEditText.getText().toString();
          targetEditText.setText(str.substring(0,maxLength));
          nextEditText.setText(str.substring(maxLength));
          nextEditText.requestFocus();
          nextEditText.setSelection(nextEditText.length());
        }
      }
    });
  }

  public Calendar getValue() {

    int year = Integer.parseInt(etYear.getText().toString());
    int month = Integer.parseInt(etMonth.getText().toString());
    int day = Integer.parseInt(etDay.getText().toString());

    // validate input.
    if (month < 1) {
      month = 1;
    }
    if (month > 12) {
      month = 12;
    }
    // month is zero index here..
    Calendar calTestDays = new GregorianCalendar(year, month-1, 1);
    int daysInMonth = calTestDays.getActualMaximum(Calendar.DAY_OF_MONTH);
    if (day < 1) {
      day = 1;
    }
    if (day > daysInMonth) {
      day = daysInMonth;
    }

    int hour = Integer.parseInt(etHour.getText().toString());
    int minute = Integer.parseInt(etMinute.getText().toString());

    if (sAmPm.getSelectedItem().toString().toUpperCase().equals("PM")){
      if (hour < 12) {
        hour += 12;
      }
    }

    hour = hour % 24;
    minute = minute % 60;

    Calendar result = Calendar.getInstance();
    result.set(year, month, day, hour, minute);

    // save the validated values into edit texts
    setValue(result);

    return result;
  }

  public void setValue(Calendar calendar) {
    String format2Digits=getContext().getString(R.string.format_2_digit);
    etYear.setText(String.format(format2Digits, calendar.get(Calendar.YEAR)));
    etMonth.setText(String.format(format2Digits, calendar.get(Calendar.MONTH) + 1));
    etDay.setText(String.format(format2Digits, calendar.get(Calendar.DAY_OF_MONTH)));
    etHour.setText(String.format(format2Digits, calendar.get(Calendar.HOUR)));
    etMinute.setText(String.format(format2Digits, calendar.get(Calendar.MINUTE)));
  }
}
