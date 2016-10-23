package com.example.pogee.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private ArrayAdapter<String> mForecastAdapter;
    private Context mContext = this.getContext();

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        mForecastAdapter = new ArrayAdapter<String>(
                //context
                getActivity(),
                //ID of list item layout
                R.layout.list_item_forecast,
                //id of textview
                R.id.list_item_forecast_textview,
                //list sof data - starts empty
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //get reference to list view and atache adapter to it
        final ListView mForecastListview = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastListview.setAdapter(mForecastAdapter);
        mForecastListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                //Toast toast = Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT);
                //toast.show();
                Intent detailIntent = new Intent(getActivity(),DetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);

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
        String locationpref = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        new FetchWeatherTask().execute(locationpref);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public class FetchWeatherTask extends AsyncTask <String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();



        /* The date/time conversion code is going to be moved outside the asynctask later,
                * so for convenience we're breaking it out into its own method now.
                */
        private String getReadableDateString(Date time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            //data is fetched in celsius by default.
            //if user selectes farenheight, values are converted here
            //we do this rather than fetching in farenheit so that the user can change this option whithout
            //having to re-fetch the ata once we start storing the vlues in database
            SharedPreferences sharedpref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedpref.getString(
                    getString(R.string.pref_tempunit_key),
                    getString(R.string.pref_tempunit_default));
            if(unitType.equals(getString(R.string.pref_tempunit_imperial))) {
                high = (high *1.8) +32;
                low = (low *1.8) +32;
            }else if (!unitType.equals(getString(R.string.pref_tempunit_default))) {
                Log.d(LOG_TAG, "unit type not found: " + unitType);
            }

            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_MAIN = "main";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            int cnt = forecastJson.getInt("cnt");

                    // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.



            String[] resultStrs = new String[numDays];
            for(int i = 0; i < cnt-1; i+=8) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;


                // Get the JSON object representing the day - Each day has 8 forecasts (every 3 hours)
                //take the one at 9am for description - but fo rfirst one take first one
                JSONObject dayForecast;
                if(i!=0){
                    dayForecast = weatherArray.getJSONObject(i + 3);
                } else {
                    dayForecast = weatherArray.getJSONObject(i);
                }




                //create a Gregorian Calendar, which is in current date
                GregorianCalendar gc = new GregorianCalendar();
                //add i dates to current date of calendar
                gc.add(GregorianCalendar.DATE, i/8);
                //get that date, format it, and "save" it on variable day
                // set to another time zone
                gc.setTimeZone(TimeZone.getTimeZone("GMT+11"));
                Date time = gc.getTime();
                day = getReadableDateString(time);


                // description is in a child array called "weather", which is 1 element long. (descr at noon)
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "main".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                //take the temperatures for that day and display max anad min
                JSONObject temperatureObject = new JSONObject();

                ArrayList<Double> tempList = new ArrayList<Double>();
                for(int j = 0; j<8 && (i+j)<(cnt-1); j++) {
                    //every 8 objects in wether array = 1 day. i is the day*8 (start from 0). j will loop through each "day"
                    temperatureObject = weatherArray.getJSONObject(i+j).getJSONObject(OWM_MAIN);
                    tempList.add(temperatureObject.getDouble(OWM_TEMPERATURE)) ;
                    //Log.e("temps", "s" + temperatureObject.getDouble(OWM_TEMPERATURE) );
                }
                if(i==0){
                    i = i- (40-cnt);
                }
                double high = Collections.max(tempList);
                double low = Collections.min(tempList);

                //Log.e("highlows", "H" +high + "low" + low );

                highAndLow = formatHighLows(high, low);
                resultStrs[(i+(40-cnt))/8] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;

        }




        @Override
        protected String[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            //number of entries returned - 8 entries for each day
            //but starting from closes 3hr so cnt changes depending on what time of day

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORCAST_BASE_URI = "http://api.openweathermap.org/data/2.5/forecast?";
                final String POSTCODE_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String KEY_PARAM = "appid";

                Uri builtUri = Uri.parse(FORCAST_BASE_URI).buildUpon()
                        .appendQueryParameter(POSTCODE_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(KEY_PARAM,BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();


                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI" + builtUri.toString());
                //check connectivity otherise offline return null
                ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                if(cm.getActiveNetworkInfo() == null ) {
                    return null;
                }


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
            try {
                return getWeatherDataFromJson(forecastJsonStr,5);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            }
            // This will only happen if there was an error getting or parsing the forecast
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (result != null) {
                mForecastAdapter.clear();
                for (String dayForecastStr : result) {
                    mForecastAdapter.add(dayForecastStr);
                }// New data is back from the server.  Hooray!
            } else {
                mForecastAdapter.clear();
                mForecastAdapter.add("Cannot get data, iMaybe no internet connection");
            }
        }


    }
}
