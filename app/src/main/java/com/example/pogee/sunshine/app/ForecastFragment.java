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
import android.support.v4.content.SharedPreferencesCompat;
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
import android.widget.Toast;

import com.example.pogee.sunshine.app.data.WeatherContract;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ForecastAdapter mForecastAdapter;
    private Context mContext = this.getContext();

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);


        mForecastAdapter = new ForecastAdapter(
                //context
                getActivity(),
                cur,0);

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



        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

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

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

}
