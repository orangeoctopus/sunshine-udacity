package com.example.pogee.sunshine.app;

/**
 * Created by Pogee on 10/11/2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pogee.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    //replaced by real list item layout
//    private String formatHighLows(double high, double low) {
//        boolean isMetric = Utility.isMetric(mContext);
//        String highLowStr = Utility.formatTemperature( high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
//        return highLowStr;
//    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
//    private String convertCursorRowToUXFormat(Cursor cursor) {
//        not need anymore sinc ein forecast fragment set colum indices of lader cursor
//         get row indices for our cursor
//        int idx_max_temp = cursor.getColumnIndex(ForecastFragment.COL_WEATHER_MAX_TEMP);
//        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
//        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
//        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
//
//        String highAndLow = formatHighLows(
//                cursor.getDouble(idx_max_temp),
//                cursor.getDouble(idx_min_temp));

//        String highAndLow = formatHighLows(
//                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
//                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP)
//        );
//
//        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
//                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
//                " - " + highAndLow;
//    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false)
        //return view;

        //multiple views:

        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
         case VIEW_TYPE_TODAY: {
          layoutId = R.layout.list_item_forecast_today;
           break;
             }
           case VIEW_TYPE_FUTURE_DAY: {
             layoutId = R.layout.list_item_forecast;
               break;
             }
          }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewholder = new ViewHolder(view);
        view.setTag(viewholder);      //set viewholder as tag of the view
        //when you read it back you have to know what you stored in there so dont abuse what you put in tag
        return view;
    }

    @Override
   public int getItemViewType(int position) {
    return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
   }

    @Override
    public int getViewTypeCount() {
    return VIEW_TYPE_COUNT;
       }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        //TextView tv = (TextView)view;
        //tv.setText(convertCursorRowToUXFormat(cursor));
        ViewHolder viewholder = (ViewHolder) view.getTag();  // read fro Tag to get back view holder objet (from new view)

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        //ImageView ivIcon = (ImageView) view.findViewById(R.id.list_item_icon);
        //ivIcon.setImageResource(R.drawable.ic_placeholder); //not a good idea keep extra icons but whatever for now
        viewholder.iconView.setImageResource(R.drawable.ic_placeholder);

        // Read date from cursor
        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
//        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
//        dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));
        viewholder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        //read forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
//        TextView DescriptionView= (TextView) view.findViewById(R.id.list_item_forecast_textview);
//        DescriptionView.setText(description);
        viewholder.descriptionView.setText(description);

        //read metric preference
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
//        TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
//        highView.setText(Utility.formatTemperature(high, isMetric));
        viewholder.highTempView.setText(Utility.formatTemperature(context, high, isMetric));

        // Read low temperature from cursor
        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
//        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
//        lowView.setText(Utility.formatTemperature(low, isMetric));
        viewholder.lowTempView.setText(Utility.formatTemperature(context, low, isMetric));



    }



    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {

        // findViewById() slows performance when frequently called
        // Even when the Adapter returns an inflated view for recycling, you still need to look up the elements and update them.
        //thus a view holder pattern is used
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}
