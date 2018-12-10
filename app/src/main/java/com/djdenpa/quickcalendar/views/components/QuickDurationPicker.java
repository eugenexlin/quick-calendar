package com.djdenpa.quickcalendar.views.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.provider.CallLog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class QuickDurationPicker extends FrameLayout {

  public interface SaveDurationHandler {
    void saveDurationMillis(long millis);
  }

  Context mContext;

  SaveDurationHandler mCallback;

  public void setCallback(SaveDurationHandler callback){
    mCallback = callback;
  }

  @BindView(R.id.et_duration)
  EditText etDuration;
  @BindView(R.id.s_duration)
  Spinner sDuration;

  @BindView(R.id.b_set)
  ImageButton bSet;
  @BindView(R.id.b_cancel)
  ImageButton bCancel;

  private long mCurrentMillis = 0;
  private int mCurrentPosition = 0;

  public QuickDurationPicker(Context context) {
    super(context);
    mContext = context;
    onCreate();
  }

  public QuickDurationPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    onCreate();
  }

  public QuickDurationPicker(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
    onCreate();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public QuickDurationPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mContext = context;
    onCreate();
  }

  public void onCreate(){

    LayoutInflater inflater = LayoutInflater.from(mContext);
    final View rootView = inflater.inflate( R.layout.view_quick_duration_picker, null, false );
    addView(rootView);

    ButterKnife.bind(this, rootView);

    String[] arr = mContext.getResources().getStringArray(R.array.duration_units);
    sDuration.setAdapter(new ArrayAdapter<>(mContext, R.layout.view_spinner_item, arr ));
    sDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        if (position != mCurrentPosition){
          long nextMultiplier = getDurationUnitMultiplier(position);
          long duration = mCurrentMillis / nextMultiplier;
          etDuration.setText(String.valueOf(duration));
          mCurrentPosition = position;
        }
      }
      @Override
      public void onNothingSelected(AdapterView<?> parentView) {
      }
    });

    bSet.setOnClickListener(v -> finishInputAndCallback());
    bCancel.setOnClickListener(v -> mCallback.saveDurationMillis(-1));

    etDuration.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
      @Override
      public void afterTextChanged(Editable s) {
        mCurrentMillis = getDurationMillis();
      }
    });
    etDuration.setOnEditorActionListener((v, actionId, event) -> {
      finishInputAndCallback();
      return true;
    });

  }

  private void finishInputAndCallback(){
    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    mCallback.saveDurationMillis(getDurationMillis());
  }

  public void focus() {
    etDuration.requestFocus();
    etDuration.selectAll();
    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(etDuration, InputMethodManager.SHOW_IMPLICIT);
  }

  public void init() {
    mCurrentMillis = 0;
  }

  public void setDurationMillis(long millis) {
    boolean shouldSetDuration = mCurrentMillis <= 0;
    mCurrentMillis = millis;
    if (shouldSetDuration) {
      setSuitableDurationUnit();
      mCurrentPosition = sDuration.getSelectedItemPosition();
    }
    long multiplier = getDurationUnitMultiplier(sDuration.getSelectedItemPosition());
    etDuration.setText(String.valueOf(mCurrentMillis/multiplier));
  }

  public long getDurationMillis(){
    long multiplier = getDurationUnitMultiplier(sDuration.getSelectedItemPosition());
    long duration = Long.parseLong(etDuration.getText().toString());
    return duration * multiplier;
  }

  public void setSuitableDurationUnit() {
    if (mCurrentMillis <= 1000) {
      sDuration.setSelection(1);
      return;
    }

    long minutes = mCurrentMillis/60000;
    if (minutes < 60) {
      sDuration.setSelection(0);
      return;
    }
    long hours = minutes / 60;
    if (hours < 48) {
      sDuration.setSelection(1);
      return;
    }
    long days = hours / 24;
    if (days < 21) {
      sDuration.setSelection(2);
      return;
    }
    // max is weeks..
    sDuration.setSelection(3);
  }

  private int getDurationUnitMultiplier(int position) {
    switch (position) {
      case 0:
        return 60000;
      case 1:
        return 60000*60;
      case 2:
        return 60000*60*24;
      default:
        return 60000*60*24*7;
    }
  }

}
