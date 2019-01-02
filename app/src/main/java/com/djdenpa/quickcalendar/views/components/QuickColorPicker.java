package com.djdenpa.quickcalendar.views.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.djdenpa.quickcalendar.R;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.flag.FlagMode;
import com.skydoves.colorpickerview.flag.FlagView;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuickColorPicker extends FrameLayout {
  Context mContext;

  String mCurrentColor;

  @BindView(R.id.ll_color)
  LinearLayout llColor;
  @BindView(R.id.ll_new_color)
  LinearLayout llColorPicker;

  @BindView(R.id.iv_color)
  ImageView ivColor;
  @BindView(R.id.et_color)
  EditText etColor;

  @BindView(R.id.color_picker)
  ColorPickerView colorPicker;
//  @BindView(R.id.brightness_slide)
//  BrightnessSlideBar brightnessSlide;

  @BindView(R.id.iv_new_color)
  ImageView ivNewColor;
  @BindView(R.id.et_new_color)
  EditText etNewColor;

  @BindView(R.id.b_set)
  ImageButton bSet;
  @BindView(R.id.b_cancel)
  ImageButton bCancel;

  public QuickColorPicker(Context context) {
    super(context);
    mContext = context;
    onCreate();
  }

  public QuickColorPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    onCreate();
  }

  public QuickColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
    onCreate();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public QuickColorPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mContext = context;
    onCreate();
  }

  public void onCreate(){

    LayoutInflater inflater = LayoutInflater.from(mContext);
    final View rootView = inflater.inflate( R.layout.view_quick_color_picker, null, false );
    addView(rootView);

    ButterKnife.bind(this, rootView);

    llColorPicker.setVisibility(GONE);

    ivColor.setOnClickListener(v -> {
      InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(getWindowToken(), 0);
      showColorSelector();
    });

    etColor.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
      @Override
      public void afterTextChanged(Editable s) {
        updateColorVisualWhenValid();
      }
    });

    bSet.setOnClickListener(v -> {
      setColor(etNewColor.getText().toString());
      hideColorSelector();
    });
    bCancel.setOnClickListener(v -> hideColorSelector());

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;
    Bitmap paletteBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.material_color, options);
    int width = paletteBitmap.getWidth()*64;
    int height = paletteBitmap.getHeight()*64;
    Bitmap scaledPalette = Bitmap.createScaledBitmap(paletteBitmap, width, height, false);
    colorPicker.setPaletteDrawable(new BitmapDrawable(getResources(), scaledPalette));
//    colorPicker.attachBrightnessSlider(brightnessSlide);
    colorPicker.setFlagView(new QuickFlag(mContext, R.layout.view_color_picker_flag));
    colorPicker.setFlagMode(FlagMode.ALWAYS);
  }

  public void showColorSelector(){
    llColor.setVisibility(GONE);
    llColorPicker.setVisibility(VISIBLE);
    colorPicker.selectCenter();
    setNewColor(tryParseColor(mCurrentColor));
  }
  public void hideColorSelector(){
    llColor.setVisibility(VISIBLE);
    llColorPicker.setVisibility(GONE);
  }

  public void updateColorVisualWhenValid(){
    String colorString = etColor.getText().toString();
    if (!colorString.startsWith("#")){
      colorString = "#" + colorString;
    }
    try {
      int color = Color.parseColor(colorString);
      ivColor.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
      mCurrentColor = colorString;
    } catch (Exception ex) {
      // do nothing
    }
  }

  public void setColor(String sColor){
    int color = tryParseColor(sColor);
    mCurrentColor = String.format("#%06X", (0xFFFFFF & color));
    etColor.setText(mCurrentColor);
    ivColor.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
  }

  public String getColor(){
    int color = tryParseColor(mCurrentColor);
    String colorString = String.format("#%06X", (0xFFFFFF & color));
    return colorString;
  }

  public void setNewColor(int color) {
    etNewColor.setText(String.format("#%06X", (0xFFFFFF & color)));
    ivNewColor.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    etNewColor.requestFocus();
  }

  protected int tryParseColor(String sColor) {
    int color;
    try {
      // we want it to exception out when null..
      // let me shortcut just this once lol
      if (!sColor.startsWith("#")){
        sColor = "#" + sColor;
      }
      color = Color.parseColor(sColor);
    } catch (Exception ex) {
      color = mContext.getColor(R.color.primaryLightColor);
    }
    return color;
  }

  protected class QuickFlag extends FlagView {

    private ImageView ivCircle;

    public QuickFlag(Context context, int layout) {
      super(context, layout);
      ivCircle = findViewById(R.id.iv_circle);
    }

    @Override
    public void onRefresh(ColorEnvelope colorEnvelope) {
//      int color = brightnessSlide.assembleColor();
      ivCircle.setColorFilter(colorEnvelope.getColor(), PorterDuff.Mode.MULTIPLY);
      setNewColor(colorEnvelope.getColor());
    }
  }
}
