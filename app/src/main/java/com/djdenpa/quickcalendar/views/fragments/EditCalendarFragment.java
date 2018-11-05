package com.djdenpa.quickcalendar.views.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.viewmodels.EditCalendarViewModel;
import com.djdenpa.quickcalendar.views.dialogs.EditCalendarNameDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditCalendarFragment extends Fragment
        implements EditCalendarNameDialog.EditCalendarNameListener {

  private Unbinder unbinder;
  @BindView(R.id.tv_calendar_name)
  TextView tvCalendarName;

  private EditCalendarViewModel viewModel;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel = ViewModelProviders.of(this.getActivity()).get(EditCalendarViewModel.class);
    viewModel.init();
    try{
      if (viewModel.getActiveCalendar().getValue().name == ""){
        viewModel.setCalendarName(getString(R.string.calendar_untitled_name));
      }
    } catch(Exception ex) {
    }


    viewModel.getActiveCalendar().observe(this, calendar -> {
      tvCalendarName.setText(calendar.name);
    });

  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    final View rootView = inflater.inflate(R.layout.fragment_edit_calendar, container, false);


    unbinder = ButterKnife.bind(this, rootView);


    tvCalendarName.setOnClickListener(v -> PromptChangeName());

    return rootView;
  }

  private void PromptChangeName(){
    EditCalendarNameDialog dialog = new EditCalendarNameDialog();
    Bundle args = new Bundle();
    args.putString(EditCalendarNameDialog.BUNDLE_CALENDAR_NAME, viewModel.getActiveCalendar().getValue().name);
    dialog.setArguments(args);
    dialog.setTargetFragment(this, 0);
    dialog.show(getFragmentManager(), "EDIT_NAME");
  }

  @Override
  public void setCalendarName(String name) {
    viewModel.setCalendarName(name);
  }
}
