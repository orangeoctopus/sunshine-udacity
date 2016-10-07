package com.example.pogee.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

         //dummy data for listview
        String[] forecastArray = {
                "Today - Sunny - 88/63",
                "T0morrow - Cloudy - 72/63",
                "wednes - Cloudy - 34",
                "Thurs - Sunny - 34/43",
                "Fris - kaboom - 343",
                "satt - rainrin - 12/34",
                "Sun - LALALAL - off",

        };


        List<String> weekForecast = new ArrayList<String>(
                Arrays.asList(forecastArray));

        ArrayAdapter<String> mForecastAdapter = new ArrayAdapter<String>(
                //context
                getActivity(),
                //ID of list item layout
                R.layout.list_item_forecast,
                //id of textview
                R.id.list_item_forecast_textview,
                //list sof data
                weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //get reference to list view and atache adapter to it
        ListView mForecastListview = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListview.setAdapter(mForecastAdapter);



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
                new FetchWeatherTask().execute("2147714");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }



    public class FetchWeatherTask extends AsyncTask <String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int count = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORCAST_BASE_URI = "http://api.openweathermap.org/data/2.5/forecast?";
                final String POSTCODE_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String COUNT_PARAM = "cnt";
                final String KEY_PARAM = "appid";

                Uri builtUri = Uri.parse(FORCAST_BASE_URI).buildUpon()
                        .appendQueryParameter(POSTCODE_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(COUNT_PARAM, Integer.toString(count))
                        .appendQueryParameter(KEY_PARAM,BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();


                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI" + builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Forecase JSON String: " + forecastJsonStr);

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return null;
        }


    }
}
