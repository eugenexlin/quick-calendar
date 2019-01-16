package com.djdenpa.quickcalendar.views.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.DisplayMode;

import java.util.HashMap;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter.EVENT_BAR_HEIGHT;
import static com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter.getEventCursorHeight;
import static com.djdenpa.quickcalendar.views.adapters.CalendarWeekAdapter.getEventCursorThickness;

public class CalendarWeekViewHolder extends RecyclerView.ViewHolder {

  @BindView(R.id.cl_calendar_week)
  ConstraintLayout clCalendarWeeks;

  @BindView(R.id.g_day_0)
  Guideline gDay0;
  @BindView(R.id.g_day_1)
  Guideline gDay1;
  @BindView(R.id.g_day_2)
  Guideline gDay2;
  @BindView(R.id.g_day_3)
  Guideline gDay3;
  @BindView(R.id.g_day_4)
  Guideline gDay4;
  @BindView(R.id.g_day_5)
  Guideline gDay5;
  @BindView(R.id.g_day_6)
  Guideline gDay6;
  @BindView(R.id.g_day_7)
  Guideline gDay7;

  @BindView(R.id.tv_day_0)
  TextView tvDay0;
  @BindView(R.id.tv_day_1)
  TextView tvDay1;
  @BindView(R.id.tv_day_2)
  TextView tvDay2;
  @BindView(R.id.tv_day_3)
  TextView tvDay3;
  @BindView(R.id.tv_day_4)
  TextView tvDay4;
  @BindView(R.id.tv_day_5)
  TextView tvDay5;
  @BindView(R.id.tv_day_6)
  TextView tvDay6;

  @BindView(R.id.iv_cursor)
  ImageView ivCursor;
  @BindView(R.id.iv_event_cursor)
  ImageView ivEventCursor;
  @BindView(R.id.iv_month_divider)
  ImageView ivVerticalDivider;

  Context mContext;

  private int previousEventLocalId;

  TouchDateHandler mTouchHandler;
  private int mCurrentPosition;

  public void setCurrentPosition(int pos) {
    mCurrentPosition = pos;
  }

  public interface TouchDateHandler {
    // position = position in adapter
    // index is column of calender 0 to 6
    void handleTouchDate(int position, int index);
    void handleReleaseDate(int position, int index);
  }

  public void setTouchHandler(TouchDateHandler handler) {
    mTouchHandler = handler;
  }


  private HashMap<String, Guideline> mDynamicGuidelines = new HashMap<>();

  // this list shall hold hidden constraint layouts that
  private LinkedList<CalendarEventViewManager> mRecycledEventViews = new LinkedList<>();
  // this list shall hold all created layouts
  private LinkedList<CalendarEventViewManager> mEventViews = new LinkedList<>();


  public CalendarWeekViewHolder(View itemView, Context context) {
    super(itemView);

    ButterKnife.bind(this, itemView);

    ViewGroup.LayoutParams params = ivEventCursor.getLayoutParams();
    params.height = EVENT_BAR_HEIGHT + getEventCursorHeight();
    ivEventCursor.setLayoutParams(params);

    clCalendarWeeks.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        int index= (int) (event.getX()*7/clCalendarWeeks.getWidth());
        switch(event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            mTouchHandler.handleTouchDate(mCurrentPosition, index);
            break;
          case MotionEvent.ACTION_UP:
            mTouchHandler.handleReleaseDate(mCurrentPosition, index);
            break;
        }
        return false;
      }
    });

    mContext = context;
  }

  public Guideline getGuideline(int position){
    switch(position) {
      case 0:
        return gDay0;
      case 1:
        return gDay1;
      case 2:
        return gDay2;
      case 3:
        return gDay3;
      case 4:
        return gDay4;
      case 5:
        return gDay5;
      case 6:
        return gDay6;
      case 7:
        return gDay7;
      default:
        return gDay0;
    }
  }

  public void setVerticalDividerPosition(int position) {
    Guideline guideline = getGuideline(position);
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clCalendarWeeks);
    constraintSet.connect(ivVerticalDivider.getId(), ConstraintSet.END, guideline.getId(), ConstraintSet.START, 0);
    constraintSet.applyTo(clCalendarWeeks);
  }

  public Guideline getHighResWeekGuideline(int scaledPosition, int multiplier) {
    // first check if it divides well
    double simplifiedPosition = (double) scaledPosition / (double) multiplier;
    //close enough to an integer.
    if (Math.abs(simplifiedPosition - Math.floor(simplifiedPosition)) < 0.01) {
      return (getGuideline((int)simplifiedPosition));
    }

    String key = "" + scaledPosition + "_" + multiplier;

    if (!mDynamicGuidelines.containsKey(key)){
      Guideline guideline = new Guideline(mContext);
      guideline.setId(View.generateViewId());
      guideline.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
      guideline.setGuidelinePercent((float)scaledPosition / (float) multiplier / 7.0f);
      clCalendarWeeks.addView(guideline);

      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(clCalendarWeeks);

      constraintSet.create(guideline.getId(), ConstraintSet.VERTICAL_GUIDELINE);

      constraintSet.applyTo(clCalendarWeeks);
      mDynamicGuidelines.put(key, guideline);
    }

    return mDynamicGuidelines.get(key);
  }

  public Guideline getHighResDayGuideline(int position, int multiplier) {
    return getHighResWeekGuideline(position*7, multiplier);
  }

  // recycleAndHide a view's visibility and
  public void RecycleViewToPool(CalendarEventViewManager eventItem) {
    eventItem.recycleAndHide();

    mRecycledEventViews.push(eventItem);
  }

  public CalendarEventViewManager getViewFromPoolOrCreate(Context context, @NonNull CalendarWeekViewHolder holder) {
    if (mRecycledEventViews.size() > 0) {
      CalendarEventViewManager result = mRecycledEventViews.poll();
      return result;
    }
    CalendarEventViewManager manager = new CalendarEventViewManager(context, holder);
    mEventViews.push(manager);
//    manager.ivEventBlock.setOnClickListener(v -> mTouchHandler.handleTouchDate(-1, -1));
    manager.ivEventBlock.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            mTouchHandler.handleTouchDate(-1, -1);
            break;
        }
        return false;
      }
    });
    return manager;
  }


  public TextView getDayTextField(int position){
    switch(position) {
      case 0:
        return tvDay0;
      case 1:
        return tvDay1;
      case 2:
        return tvDay2;
      case 3:
        return tvDay3;
      case 4:
        return tvDay4;
      case 5:
        return tvDay5;
      case 6:
        return tvDay6;
      default:
        return tvDay0;
    }
  }

  public ConstraintLayout getParentLayout(){
    return clCalendarWeeks;
  }

  public void resynchronizeDateNumberVisuals(int currentMonth, DisplayMode displayMode, int cursorPosition, int cursorIndex) {

    if (displayMode == DisplayMode.ROW_PER_DAY) {
      cursorIndex = -1;
    }

    for (int i = 0; i < 7; i++) {
      TextView view = getDayTextField(i);
      if (view == null) {
        continue;
      }
      if (view.getTag(R.id.tag_tv_month_key) == null){
        continue;
      }
      int viewMonth = (int) view.getTag(R.id.tag_tv_month_key);
      boolean shouldNotFade = false;

      if (getAdapterPosition() == cursorPosition &&
          ((displayMode == DisplayMode.ROW_PER_DAY && i == 0) ||
            (displayMode == DisplayMode.ROW_PER_WEEK && i == cursorIndex))) {

        shouldNotFade = true;
        view.setTextColor(mContext.getColor(R.color.white));
        view.setTypeface(null, Typeface.BOLD);
        Drawable drawable = mContext.getDrawable(R.drawable.circle);
        drawable.setColorFilter(new PorterDuffColorFilter(
                mContext.getColor(R.color.secondaryColor),
                PorterDuff.Mode.MULTIPLY));
        view.setBackground(drawable);

      } else if (view.getTag(R.id.tag_tv_is_today_key) != null) {

        shouldNotFade = true;
        view.setTextColor(mContext.getColor(R.color.white));
        view.setTypeface(null, Typeface.BOLD);
        Drawable drawable = mContext.getDrawable(R.drawable.circle);
        drawable.setColorFilter(new PorterDuffColorFilter(
                mContext.getColor(R.color.primaryColor),
                PorterDuff.Mode.MULTIPLY));
        view.setBackground(drawable);

      } else {

        view.setTextColor(mContext.getColor(R.color.darker_gray));
        view.setTypeface(null, Typeface.NORMAL);
        view.setBackground(null);

      }
      if (viewMonth == currentMonth || shouldNotFade) {
        view.setAlpha(1f);
      } else {
        view.setAlpha(0.3f);
      }
    }
    renderCursor(cursorPosition, cursorIndex);
  }

  // index of cursor, or -1 for entire week
  private int mCursorIndex;
  // enabled means the cursor is showing somewhere on this item
  private boolean mCursorEnabled;

  public void renderCursor(int position, int index){
    if (!mCursorEnabled){
      if (mCurrentPosition != position){
        // nothing to do
      } else {
        //add cursor based on index
        setCursor(index);
      }
    } else { // is enabled
      if (mCurrentPosition != position){
        clearCursor();
      } else {
        //add cursor based on index
        setCursor(index);
      }
    }
  }

  private void setCursor(int index){
    if (mCursorEnabled && mCursorIndex == index) {
      return;
    }
    mCursorIndex = index;
    mCursorEnabled = true;
    if (index < 0) {
      setCursorWhole();
      return;
    }
    // cursor is showing, but we nee to recycleAndHide, so HIDE
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clCalendarWeeks);
    constraintSet.connect(
            ivCursor.getId(),
            ConstraintSet.START,
            getGuideline(index).getId(),
            ConstraintSet.START, 0);
    constraintSet.connect(
            ivCursor.getId(),
            ConstraintSet.END,
            getGuideline(index+1).getId(),
            ConstraintSet.START, 0);
    constraintSet.applyTo(clCalendarWeeks);

    TextView tv = getDayTextField(index);
    tv.setBackground(null);
    Drawable drawable = mContext.getDrawable(R.drawable.circle);
    drawable.setColorFilter(new PorterDuffColorFilter(mContext.getColor(R.color.secondaryColor), PorterDuff.Mode.MULTIPLY));
    tv.setBackground(drawable);
  }
  private void setCursorWhole(){
    // cursor is showing, but we nee to recycleAndHide, so HIDE
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clCalendarWeeks);
    constraintSet.connect(
            ivCursor.getId(),
            ConstraintSet.START,
            getGuideline(0).getId(),
            ConstraintSet.START, 0);
    constraintSet.connect(
            ivCursor.getId(),
            ConstraintSet.END,
            getGuideline(7).getId(),
            ConstraintSet.START, 0);
    constraintSet.applyTo(clCalendarWeeks);

    TextView tv = getDayTextField(0);
    tv.setBackground(null);
    Drawable drawable = mContext.getDrawable(R.drawable.circle);
    drawable.setColorFilter(new PorterDuffColorFilter(mContext.getColor(R.color.secondaryColor), PorterDuff.Mode.MULTIPLY));
    tv.setBackground(drawable);
  }
  private void clearCursor(){
    mCursorEnabled = false;
    // cursor is showing, but we nee to recycleAndHide, so HIDE
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clCalendarWeeks);
    constraintSet.connect(
            ivCursor.getId(),
            ConstraintSet.START,
            getGuideline(0).getId(),
            ConstraintSet.START, 0);
    constraintSet.connect(
            ivCursor.getId(),
            ConstraintSet.END,
            getGuideline(0).getId(),
            ConstraintSet.START, 0);
    constraintSet.applyTo(clCalendarWeeks);

    for (int i = 0; i < 7; i++) {
      TextView tv = getDayTextField(i);
      if (tv.getTag(R.id.tag_tv_is_today_key) == null){
        tv.setBackground(null);
      }
    }

  }
  public void resetEventCursor(){
    previousEventLocalId = -1;
  }
  public boolean setEventCursor(int eventLocalId, boolean requestFocus) {
    if (previousEventLocalId == eventLocalId) {
      return false;
    }
    previousEventLocalId = eventLocalId;
    for (CalendarEventViewManager vm : mEventViews) {
      if (vm.mEventLocalId == eventLocalId &&
              vm.guidelineStart != null &&
              vm.guidelineEnd != null) {

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(clCalendarWeeks);
        constraintSet.connect(
                ivEventCursor.getId(),
                ConstraintSet.START,
                vm.guidelineStart.getId(),
                ConstraintSet.START, 0);
        constraintSet.connect(
                ivEventCursor.getId(),
                ConstraintSet.END,
                vm.guidelineEnd.getId(),
                ConstraintSet.END, 0);
        constraintSet.connect(
                ivEventCursor.getId(),
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP, vm.marginTop - getEventCursorThickness());
        constraintSet.applyTo(clCalendarWeeks);
        if (requestFocus) {
          ivEventCursor.requestFocus();
        }
        return true;
      }
    }
    // did not find, so put off screen;
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(clCalendarWeeks);
    constraintSet.connect(
            ivEventCursor.getId(),
            ConstraintSet.START,
            getGuideline(0).getId(),
            ConstraintSet.START, 0);
    constraintSet.connect(
            ivEventCursor.getId(),
            ConstraintSet.END,
            getGuideline(0).getId(),
            ConstraintSet.START, 0);
    constraintSet.applyTo(clCalendarWeeks);
    constraintSet.applyTo(clCalendarWeeks);
    return false;
  }
}
