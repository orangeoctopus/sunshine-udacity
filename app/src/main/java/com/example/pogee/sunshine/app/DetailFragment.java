package com.example.pogee.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pogee.sunshine.app.data.WeatherContract;
import com.example.pogee.sunshine.app.data.WeatherContract.WeatherEntry;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #Sunshine App";
        private String mForecastStr;

        private ShareActionProvider mShareActionProvider;
        private String mForecast;
        private Uri mUri;


        private static final int DETAIL_LOADER = 0;
        static final String DETAIL_URI = "URI";

        private static final String[] FORECAST_COLUMNS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATE,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_WIND_SPEED,
                WeatherEntry.COLUMN_DEGREES,
                WeatherEntry.COLUMN_WEATHER_ID,
                // This works because the WeatherProvider returns location data joined with
                // weather data, even though they're stored in two different tables.
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
         };

        // these constants correspond to the projection defined above, and must change if the
               // projection changes
        private static final int COL_WEATHER_ID = 0;
        private static final int COL_WEATHER_DATE = 1;
        private static final int COL_WEATHER_DESC = 2;
        private static final int COL_WEATHER_MAX_TEMP = 3;
        private static final int COL_WEATHER_MIN_TEMP = 4;
        private static final int COL_HUMIDITY = 5;
        public static final int COL_WEATHER_PRESSURE = 6;
        public static final int COL_WEATHER_WIND_SPEED = 7;
        public static final int COL_WEATHER_DEGREES = 8;
        public static final int COL_WEATHER_CONDITION_ID = 9;

        //declare all the items from content_detailxml
        private ImageView mIconView;
        private TextView mFriendlyDateView;
        private TextView mDateView;
        private TextView mDescriptionView;
        private TextView mHighTempView;
        private TextView mLowTempView;
        private TextView mHumidityView;
        private TextView mWindView;
        private TextView mPressureView;

    public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
         if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            }

        View rootView = inflater.inflate(R.layout.content_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
         mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;

        //reference the views we need later on so no need to traverese the whole view hierarchy again everytime loader loads


//       Intent intent = getActivity().getIntent();
//        View rootview = inflater.inflate(R.layout.content_detail, container, false);
//        if (intent != null) {
//           mForecastStr = intent.getDataString();
//        }
//        if (null != mForecastStr) {
//            ((TextView) rootview.findViewById(R.id.detail_text))
//                    .setText(mForecastStr);
//        }
        //return rootview;
    }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            Log.v(LOG_TAG, "In onLoadFinished");
            if (data != null && data.moveToFirst()) {
                // Read weather condition ID from cursor
                int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
                // Use placeholder Image
                //mIconView.setImageResource(R.drawable.ic_placeholder);
                mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

                //read description
                String description = data.getString(COL_WEATHER_DESC);
                mDescriptionView.setText(description);

                //read day and date from cursor
                long dateInMillis = data.getLong(COL_WEATHER_DATE);
                mFriendlyDateView.setText(Utility.getDayName(getActivity(), dateInMillis));
                String formatteddate = Utility.getformattedDate(dateInMillis, "MMM d");
                mDateView.setText(formatteddate);

                //get metric from pref
                boolean isMetric = Utility.isMetric(getActivity());

                //read high and low temperatures
                double high = data.getDouble(COL_WEATHER_MAX_TEMP);
                double low = data.getDouble(COL_WEATHER_MIN_TEMP);
                mHighTempView.setText(Utility.formatTemperature(getActivity(), high, isMetric));
                mLowTempView.setText(Utility.formatTemperature(getActivity(), low, isMetric));

                // read humulity form cursor
                double humidity = data.getDouble(COL_HUMIDITY);
                mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));


                //read wind from curosr
                double windspeed = data.getDouble(COL_WEATHER_WIND_SPEED);
                float winddirn = data.getFloat(COL_WEATHER_DEGREES);
                mWindView.setText(Utility.getFormattedWind(getActivity(),windspeed,winddirn));


                // read pressure from cursor
                double pressure = data.getDouble(COL_WEATHER_PRESSURE);
                mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

                //string for share provider
                mForecastStr = "On " + formatteddate + " it will be " + description + " with high " + high + " and low " + low;



            }


                // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                //update shareaction provider if it already exists
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
                }
//            if (!data.moveToFirst()) { return; }
//
//            String dateString = Utility.formatDate(
//                    data.getLong(COL_WEATHER_DATE));
//
//            String weatherDescription =
//            data.getString(COL_WEATHER_DESC);
//            boolean isMetric = Utility.isMetric(getActivity());
//
//            String high = Utility.formatTemperature(getContext(),
//                    data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
//
//            String low = Utility.formatTemperature(getContext(),
//                    data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
//            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
//
//            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
//            detailTextView.setText(mForecast);



        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
             if (intent == null | intent.getData() ==null) {   //If DetailFragment is created without a uri (as in intent.data() == null), it should not try to create a loader
                      return null;
                      }

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            //no data that we are holding onto that needs to be cleaned up so no need to put swap a null

        }

        void onLocationChanged( String newLocation ) {
            // replace the uri, since the location has changed
            Uri uri = mUri;
            if (null != uri) {
                long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
                Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
                mUri = updatedUri;
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            //inflater.inflate(R.menu.menu_detail,menu);
            //locate menu item with shareacitonprovider
            MenuItem item = menu.findItem(R.id.action_share);
            //feth and store share actiona provider
            mShareActionProvider= (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            //app:actionProviderClass="android.support.v7.widget.ShareActionProvider"

            //attach an intent to this Sharectionprovider. you can update this at anytime
            //like when the user selects a new piece of data they might like to share
            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }else {
                Log.d(LOG_TAG, "What the heck is shareactionprovider null?");
            }

        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr+FORECAST_SHARE_HASHTAG);
            return shareIntent;

        }


    }



