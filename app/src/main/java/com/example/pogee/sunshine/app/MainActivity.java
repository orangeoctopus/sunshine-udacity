package com.example.pogee.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{

    String mLocation;
    //private static final String FORECASTFRAGMENT_TAG = "FFTAG";
    //We no longer need the FORCASTFRAGMENT tag since we’re no longer explicitly creating the ForcastFragment.
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mLocation = Utility.getPreferredLocation(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment, new ForecastFragment(), FORECASTFRAGMENT_TAG)
//                    .commit();
//        }
        //now the forcast fragment is statically added - only detail fragment dynamically added

        //To know what case it is, have your MainActivity check whether or not the layout contains a
        // view with the id weather_detail_container. If it does,
        // it’s a two pane layout so set mTwoPane to true, otherwise you can set it to false. Also, if it’s a two pane layout,
        // you’ll need to add a new DetailFragment

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                //Why check savedInstanceState to equal null? Well if we rotate the phone, the system saves the fragment state
                // in the saved state bundle and is smart enough to restore this state.
                //Therefore, if the saved state bundle is not null, the system already has the fragment it needs and you shouldn’t go adding another one.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f); //no shadow on action bar for onepane
        }

        ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane); //today special layout if not in two pane mode

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_map:
                openPreferredLocationOnMap();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
             // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

             DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
            } else {
            Intent intent = new Intent(this, DetailActivity.class)
                                     .setData(contentUri);
            startActivity(intent);
        }
    }

    private void openPreferredLocationOnMap() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        //dogey converting postcode to city name
        if(location.equals("1835848")) {
            location = "Seoul";
        } else if (location.equals("1819729")){
            location = "Hong Kong";
        }else {
            location = "Sydney";
        }
        Uri geoLocation = Uri.parse("geo:0,0?:").buildUpon()
                .appendQueryParameter("q", location)
                .build();
        Intent mapintent = new Intent(Intent.ACTION_VIEW);
        mapintent.setData(geoLocation);
        if(getIntent().resolveActivity(getPackageManager()) != null) {
            startActivity(mapintent);
        }
    }
}
