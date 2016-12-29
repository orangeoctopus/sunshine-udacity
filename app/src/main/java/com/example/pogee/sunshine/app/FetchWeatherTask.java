/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.pogee.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.pogee.sunshine.app.data.WeatherContract;
import com.example.pogee.sunshine.app.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;

public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private ArrayAdapter<String> mForecastAdapter;
    private final Context mContext;

    public FetchWeatherTask(Context context) {
        //removed from constructor parameter: , ArrayAdapter<String> forecastAdapter
        mContext = context;
        //removed: mForecastAdapter = forecastAdapter;
    }

    private boolean DEBUG = true;


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        long locationId;
        // First, check if the location with this city name exists in the db
        Cursor locationCursor = mContext.getContentResolver().query(
                              WeatherContract.LocationEntry.CONTENT_URI,
                              new String[]{WeatherContract.LocationEntry._ID},
                              WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                              new String[]{locationSetting},
                              null);

        if (locationCursor.moveToFirst()) {
                      int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                       locationId = locationCursor.getLong(locationIdIndex);
                    } else {
                        // Now that the content provider is set up, inserting rows of data is pretty simple.
                     // First create a ContentValues object to hold the data you want to insert.
                           ContentValues locationValues = new ContentValues();
                          // Then add the data, along with the corresponding name of the data type,
                         // so the content provider knows what kind of value is being inserted.
                        locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
                       locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
                       locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
                       locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

                                // Finally, insert location data into the database.
                                     Uri insertedUri = mContext.getContentResolver().insert(
                                     WeatherContract.LocationEntry.CONTENT_URI,
                                     locationValues
                                      );

                               // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
                               locationId = ContentUris.parseId(insertedUri);
                 }

                        locationCursor.close();
               // Wait, that worked?  Yes!
                        return locationId;
    }

    /*
        Students: This code will allow the FetchWeatherTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */
//    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
//        // return strings to keep UI functional for now
//        String[] resultStrs = new String[cvv.size()];
//        for ( int i = 0; i < cvv.size(); i++ ) {
//            ContentValues weatherValues = cvv.elementAt(i);
//            String highAndLow = formatHighLows(
//                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MAX_TEMP),
//                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MIN_TEMP));
//            resultStrs[i] = getReadableDateString(
//                    weatherValues.getAsLong(WeatherEntry.COLUMN_DATE)) +
//                    " - " + weatherValues.getAsString(WeatherEntry.COLUMN_SHORT_DESC) +
//                    " - " + highAndLow;
//        }
//        return resultStrs;
//    }

//    private String getDayFromUnix(Long dateUnix) {
//        Date date = new Date(dateUnix*1000L); // *1000 is to convert seconds to milliseconds
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // the format of your date
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT+11")); // give a timezone reference for formating (see comment at the bottom
//
//        return sdf.format(date);
//    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr,
                                            String locationSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_WIND = "wind";

        // All temperatures are children of the "temp" object.
        final String OWM_MAIN = "main";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        final String OWM_TIME = "dt";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());



            int cnt = forecastJson.getInt("cnt");
            int numDays = 5;

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < cnt; i+=8) {

                // These are the values that will be collected.
                long day;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                int weatherId;
                // For now, using the format "Day, description, hi/low"
                //String dayString;
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





//                //create a Gregorian Calendar, which is in current date
//                GregorianCalendar gc = new GregorianCalendar();
//                //add i dates to current date of calendar
//                gc.add(GregorianCalendar.DATE, i/8);
//                //get that date, format it, and "save" it on variable day
//                // set to another time zone
//                gc.setTimeZone(TimeZone.getTimeZone("GMT+11"));
//                day = gc.getTime().getTime();
//                dayString = DateFormat.getDateInstance(DateFormat.SHORT).format(gc.getTime());;
//
                  //create a Gregorian Calendar, which is in current date
                GregorianCalendar gc = new GregorianCalendar();
//                //add i dates to current date of calendar
//                //get that date, format it, and "save" it on variable day
//                // set to another time zone

                Date forecastTime = new Date(dayForecast.getLong(OWM_TIME)*1000);
                gc.setTimeZone(TimeZone.getTimeZone("GMT+10"));
                gc.setTime(forecastTime);
                gc.add(GregorianCalendar.HOUR, 10);


                day = gc.getTime().getTime();
//                dayString = DateFormat.getDateInstance(DateFormat.SHORT).format(gc.getTime());;

//                day = dayForecast.getLong(OWM_TIME);
//                day = getDayFromUnix(dayForecast.getLong(OWM_TIME));
//                Log.e(LOG_TAG,day);



                // description is in a child array called "weather", which is 1 element long. (descr at noon)
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);


                // Temperatures are in a child object called "main".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                //take the temperatures for that day and display max anad min
                JSONObject temperatureObject;
                JSONObject windObject;

                ArrayList<Double> tempList = new ArrayList<Double>();
                double pressureSum = 0;

                double speedSum = 0;
                int humiditySum = 0;
                double directionSum = 0;
                int numtocnt = 8;
                        if(i==0) {
                            numtocnt = 8-(40-cnt);
                            if(numtocnt<0) {
                                //becuse for some reason theres only like 27 entries in api call
                                numtocnt +=8;
                            }
                        }

                for(int j = 0; j<numtocnt && (i+j)<(cnt); j++) {
                    //every 8 objects in weather array = 1 day. i is the day*8 (start from 0). j will loop through each "day"
                    temperatureObject = weatherArray.getJSONObject(i+j).getJSONObject(OWM_MAIN);
                    tempList.add(temperatureObject.getDouble(OWM_TEMPERATURE)) ;
                    Log.e("testata", "Day: " +DateFormat.getDateInstance(DateFormat.SHORT).format(gc.getTime()));
                    //Log.e("testata", "Temp: " +Double.toHexString(temperatureObject.getDouble(OWM_TEMPERATURE)));
                    Log.e("testata", "i is: "+ i + " j is" + j);
                    pressureSum += temperatureObject.getDouble(OWM_PRESSURE);
                    humiditySum += temperatureObject.getInt(OWM_HUMIDITY);
                    windObject = weatherArray.getJSONObject(i+j).getJSONObject(OWM_WIND);
                    speedSum += windObject.getDouble(OWM_WINDSPEED);
                    directionSum += windObject.getDouble(OWM_WIND_DIRECTION);
                }
                if(i==0){
                    i = i- (40-cnt);
                    if(i<-8) {
                        //becuse for some reason theres only like 27 entries in api call
                        i +=8;
                    }
                }
                if(tempList.size() != 0) {
                    high = Collections.max(tempList);
                    low = Collections.min(tempList);
                }else{
                    high = 50;
                    low = 50;
                }

                windSpeed = speedSum/numtocnt;
                pressure = pressureSum/numtocnt;
                windDirection = directionSum/numtocnt;
                humidity = humiditySum/numtocnt;
                //Log.e("highlows", "H" +high + "low" + low );
//                not needed anymore - now handled by forecast adapter
//                highAndLow = formatHighLows(high, low);
//                resultStrs[(i+(40-cnt))/8] = day + " - " + description + " - " + highAndLow;

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherEntry.COLUMN_DATE, day);
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);

            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);  //convert vector to array
                inserted = mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, cvArray);

            }

//             Changed after to loader :
//                              mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, cvArray);
//            }
//
//
//
//            // Sort order:  Ascending, by date.
//            // but the date is in UTC which is behind my Australian time
//            String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
//            Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
//                    locationSetting, System.currentTimeMillis());
//
//            // Students: Uncomment the next lines to display what what you stored in the bulkInsert
//
//            Cursor cur = mContext.getContentResolver().query(weatherForLocationUri,
//                    null, null, null, sortOrder);
//
//            cVVector = new Vector<ContentValues>(cur.getCount());
//            if ( cur.moveToFirst() ) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    DatabaseUtils.cursorRowToContentValues(cur, cv);
//                    cVVector.add(cv);
//                } while (cur.moveToNext());
//            }
//
//            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");
//
//            resultStrs = convertContentValuesToUXFormat(cVVector);
//            return resultStrs;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        //return null;
    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String locationQuery = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
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

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
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
                return null;
            }
            forecastJsonStr = buffer.toString();
            getWeatherDataFromJson(forecastJsonStr, locationQuery);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }
}