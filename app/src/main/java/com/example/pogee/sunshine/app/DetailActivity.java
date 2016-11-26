package com.example.pogee.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pogee.sunshine.app.data.WeatherContract.WeatherEntry;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailActivityFragment())
                    .commit();
        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_detail,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #Sunshine App";
        private String mForecastStr;

        private ShareActionProvider mShareActionProvider;
        private String mForecast;


        private static final int DETAIL_LOADER = 0;

        private static final String[] FORECAST_COLUMNS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATE,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
         };

        // these constants correspond to the projection defined above, and must change if the
               // projection changes
        private static final int COL_WEATHER_ID = 0;
        private static final int COL_WEATHER_DATE = 1;
        private static final int COL_WEATHER_DESC = 2;
        private static final int COL_WEATHER_MAX_TEMP = 3;
        private static final int COL_WEATHER_MIN_TEMP = 4;

        public DetailActivityFragment() {
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
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }

            String dateString = Utility.formatDate(
                    data.getLong(COL_WEATHER_DATE));

            String weatherDescription =
            data.getString(COL_WEATHER_DESC);
            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

            String low = Utility.formatTemperature(
                    data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
            detailTextView.setText(mForecast);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            //update shareaction provider if it already exists
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }


        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
             if (intent == null) {
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

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            //inflater.inflate(R.menu.menu_detail,menu);
            //locate menu item with shareacitonprovider
            MenuItem item = menu.findItem(R.id.action_share);
            //feth and store share actiona provider
            ShareActionProvider shareActionProvider= (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            //app:actionProviderClass="android.support.v7.widget.ShareActionProvider"

            //attach an intent to this Sharectionprovider. you can update this at anytime
            //like when the user selects a new piece of data they might like to share
            // If onLoadFinished happens before this, we can go ahead and set the share intent now.
            if(shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareForecastIntent());
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            Intent intent = getActivity().getIntent();
            View rootview = inflater.inflate(R.layout.fragment_detail, container, false);
            if (intent != null) {
                mForecastStr = intent.getDataString();
            }
            if (null != mForecastStr) {
               ((TextView) rootview.findViewById(R.id.detail_text))
                        .setText(mForecastStr);
            }




            return rootview;
        }
    }


}


