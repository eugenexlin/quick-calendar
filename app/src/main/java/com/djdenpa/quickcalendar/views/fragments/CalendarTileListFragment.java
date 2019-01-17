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
import com.djdenpa.quickcalendar.models.CalendarTile;
import com.djdenpa.quickcalendar.views.adapters.CalendarTileListItemAdapter;

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
  @BindView(R.id.tv_tile_header)
  TextView tvTileHeader;

  private CalendarTileListItemAdapter mCalendarTilesAdapter;

  public CalendarTileListFragment() {
    // Required empty public constructor
  }

  public static CalendarTileListFragment newInstance(String param1, String param2) {
    CalendarTileListFragment fragment = new CalendarTileListFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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

  public void setHeaderText(String text){
    tvTileHeader.setText(text);
  }

  public void setAdapterData(List<CalendarTile> data) {
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
