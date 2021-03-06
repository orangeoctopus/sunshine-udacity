package com.example.pogee.sunshine.app;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pogee.sunshine.app.data.WeatherContract;




/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private ForecastAdapter mForecastAdapter;
    private Context mContext = this.getContext();

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        mForecastAdapter = new ForecastAdapter(
                //context
                getActivity(),
                null ,0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //get reference to list view and atache adapter to it
        final ListView mForecastListview = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListview.setAdapter(mForecastAdapter);
        //not arrray adapter anymore so no onclicklistener
//        mForecastListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//
//                // String forecast = mForecastAdapter.getItem(position);
//                //Toast toast = Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT);
//                //toast.show();
//                Intent detailIntent = new Intent(getActivity(),DetailActivity.class)
//                .putExtra(Intent.EXTRA_TEXT, forecast);
//                startActivity(detailIntent);

//            }
//        });

        mForecastListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });



        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //where we create and return a cursor loader
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mForecastAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);

    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
            updateWeather();
           getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
         }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_refresh:
                updateWeather();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void updateWeather() {
//        Below changed due to not array adapter anymore:
//       FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
//        String locationpref = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                .getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String locationpref = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(locationpref);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        updateWeather();
//    }

}
