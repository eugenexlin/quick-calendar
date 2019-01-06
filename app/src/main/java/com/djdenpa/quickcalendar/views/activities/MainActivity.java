package com.djdenpa.quickcalendar.views.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.djdenpa.quickcalendar.R;
import com.djdenpa.quickcalendar.database.QuickCalendarDatabase;
import com.djdenpa.quickcalendar.utils.QuickCalendarExecutors;
import com.djdenpa.quickcalendar.utils.SharedPreferenceManager;
import com.djdenpa.quickcalendar.views.fragments.CalendarTileListFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainActivity extends AppCompatActivity {

  CalendarTileListFragment mRecentCalendarFragment;
  CalendarTileListFragment mRecentSharedCalendarFragment;

  GoogleSignInClient mGoogleSignInClient;

  private FirebaseAuth mAuth;

  private Unbinder unbinder;

  @BindView(R.id.ll_sign_out_buttons)
  LinearLayout mLogoutButtonLayout;
  @BindView(R.id.sign_in_button)
  SignInButton mSignInButton;
  @BindView(R.id.tv_account_text)
  TextView mLoginText;
  @BindView(R.id.sign_out_button)
  TextView mSignOutButton;
  @BindView(R.id.disconnect_button)
  TextView mDisconnectButton;

  private static final int RC_SIGN_IN = 9001;

  QuickCalendarDatabase mDB;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    unbinder = ButterKnife.bind(this);

    mDB = QuickCalendarDatabase.getInstance(getApplicationContext());

    // TEST LOCALE
//    Resources resources = getResources();
//    Configuration configuration = resources.getConfiguration();
//    configuration.setLocale(Locale.JAPAN);
//    resources.updateConfiguration(configuration, resources.getDisplayMetrics());

    mRecentCalendarFragment = (CalendarTileListFragment)
            getSupportFragmentManager().findFragmentById(R.id.f_recent_calendars);
    mRecentSharedCalendarFragment = (CalendarTileListFragment)
            getSupportFragmentManager().findFragmentById(R.id.f_recent_shared_calendars);

    mRecentCalendarFragment.setEmptyStateHelperText(
            getString(R.string.no_calendars_sub));
    mRecentSharedCalendarFragment.setEmptyStateHelperText(
            getString(R.string.no_shared_calendars_sub));

    mRecentCalendarFragment.setHeaderText(
            getString(R.string.tile_fragment_recent_title));
    mRecentSharedCalendarFragment.setHeaderText(
            getString(R.string.tile_fragment_recent_shared_title));



    // mRecentCalendarFragment.bindTestData();


    FloatingActionButton fab = findViewById(R.id.fab_create_new);
    fab.setOnClickListener(view -> {
      Intent intent = new Intent(MainActivity.this, EditCalendarActivity.class);
      startActivity(intent);
    });

    mSignInButton.setOnClickListener(view -> {
      Intent signInIntent = mGoogleSignInClient.getSignInIntent();
      startActivityForResult(signInIntent, RC_SIGN_IN);
    });

    mSignOutButton.setOnClickListener(view -> mGoogleSignInClient.signOut()
            .addOnCompleteListener(this, task -> processGoogleAccount(null)));

    mDisconnectButton.setOnClickListener(view -> mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(this, task -> processGoogleAccount(null)));


    String serverClientId = getString(R.string.google_client_id);
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build();
    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    mAuth = FirebaseAuth.getInstance();

  }

  protected void processGoogleAccount(GoogleSignInAccount account){
    SharedPreferenceManager prefMan = new SharedPreferenceManager(this);

    if (account == null) {

      mSignInButton.setVisibility(View.VISIBLE);
      mLogoutButtonLayout.setVisibility(View.GONE);
      mLoginText.setText(R.string.sign_in_please);

    } else {

      String idToken = account.getIdToken();

      prefMan.setUserIdToken(idToken);
      prefMan.setUserEmail(account.getEmail());

      mLogoutButtonLayout.setVisibility(View.VISIBLE);
      mSignInButton.setVisibility(View.GONE);
      mLoginText.setText(getString(R.string.signed_in_as_fmt, account.getEmail()));

      AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
      mAuth.signInWithCredential(credential);

      FirebaseUser fu = mAuth.getCurrentUser();
      if (fu != null) {
        prefMan.setUserId(fu.getUid());
      }

      /*
this Firebase setting will restrict currently logged in user to their user id folder.
This was for testing, but we can not use this because we need a shared space.

{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
       */
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    processGoogleAccount(account);
  }

  @Override
  protected void onResume() {
    super.onResume();

    BindCalendarData();
  }

  private void BindCalendarData() {
    mRecentCalendarFragment.setLoading(true);
    QuickCalendarExecutors.getInstance().diskIO().execute(() -> {
      mDB.calendarDao().loadAllCalendars().observe(this, calendarList -> {
        QuickCalendarExecutors.getInstance().mainThread().execute(() -> {
          mRecentCalendarFragment.setAdapterData(calendarList);
          mRecentCalendarFragment.setLoading(false);
        });
      });
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      try {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        GoogleSignInAccount account = task.getResult(ApiException.class);
        processGoogleAccount(account);
      } catch (ApiException e) {
        Toast.makeText(this, "Exception during account sign in. " + e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

  }

}
