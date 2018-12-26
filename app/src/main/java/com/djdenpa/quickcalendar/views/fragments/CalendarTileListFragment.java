package com.djdenpa.quickcalendar.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.models.Calendar;
import com.djdenpa.quickcalendar.models.CalendarInfo;
import com.djdenpa.quickcalendar.utils.MockCalendarDataGenerator;
import com.djdenpa.quickcalendar.views.adapters.CalendarTileListItemAdapter;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CalendarTileListFragment extends Fragment {
  @BindView(R.id.tv_no_calendars_sub)
  TextView tvNoCalendarsSub;
  private Unbinder unbinder;
  @BindView(R.id.rv_calendar_tiles)
  RecyclerView rvCalendarTiles;
  @BindView(R.id.cl_empty_state)
  ConstraintLayout clEmptyState;
  @BindView(R.id.pb_loading)
  ProgressBar pbLoading;

  private CalendarTileListItemAdapter mCalendarTilesAdapter;

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";



  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;



  public CalendarTileListFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment CalendarTileListFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static CalendarTileListFragment newInstance(String param1, String param2) {
    CalendarTileListFragment fragment = new CalendarTileListFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_calendar_tile_list, container, false);
    unbinder = ButterKnife.bind(this, view);
    mCalendarTilesAdapter = new CalendarTileListItemAdapter();
    rvCalendarTiles.setAdapter(mCalendarTilesAdapter);

    final LinearLayoutManager layoutManager
            = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);

    rvCalendarTiles.setLayoutManager(layoutManager);

    return view;
  }



  public void setEmptyStateHelperText(String message){
    tvNoCalendarsSub.setText(message);
  }

  public void bindTestData(){
    MockCalendarDataGenerator junk = new MockCalendarDataGenerator();
    setAdapterData(junk.getMockCalendars());
  }

  public void setAdapterData(List<Calendar> data) {
    mCalendarTilesAdapter.setData(data);
    mCalendarTilesAdapter.notifyDataSetChanged();
    if (data.size() <= 0 ) {
      rvCalendarTiles.setVisibility(View.GONE);
      clEmptyState.setVisibility(View.VISIBLE);
    } else {
      rvCalendarTiles.setVisibility(View.VISIBLE);
      clEmptyState.setVisibility(View.GONE);
    }
  }

  public void setLoading(boolean isLoading){
    if (isLoading) {
      pbLoading.setVisibility(View.VISIBLE);
    } else {
      pbLoading.setVisibility(View.GONE);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (unbinder != null){
      unbinder.unbind();
    }
  }
}
