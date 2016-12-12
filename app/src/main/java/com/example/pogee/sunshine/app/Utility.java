package com.example.pogee.sunshine.app;

/**
 * Created by Pogee on 10/11/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_tempunit_key),
                context.getString(R.string.pref_tempunit_default))
                .equals(context.getString(R.string.pref_tempunit_default));
    }

    static String formatTemperature(Context context,double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        //String.format("%.0f", temp)
        // <string name="format_temperature"><xliff:g id="temp">%1.0f</xliff:g>\u00B0</string>
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    public static String getFriendlyDayString( Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8
        //changed:
        //Tomorr: "Tommorrow, June 9
        //ext days Mon Jun8

        //my dogey method
//        Calendar calNow = Calendar.getInstance();
//        Calendar cal = Calendar.getInstance();
//
//        Calendar calTmr = Calendar.getInstance();
//        calTmr.add(Calendar.DAY_OF_YEAR, +1);
//
//        cal.setTimeInMillis(dateInMillis*1000);

        // better:
//        Calendar calendar = Calendar.getInstance();
//        int currentJulianDay = calendar.get(Calendar.DAY_OF_YEAR);
//        calendar.setTimeInMillis(dateInMillis);
//        int julianDay = calendar.get(Calendar.DAY_OF_YEAR);
        // julianDay < currentJulianDay + 7

// calendar.getTime().getTime() <- to get fromo date to millis


        return getDayName(context, dateInMillis) + ", " + getformattedDate(dateInMillis, "MMM d");


    }

    static String getformattedDate(long millis, String format) {
        Date d = new Date (millis);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(d);
    }

    static String getDayName(Context context, long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        int currentJulianDay = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTimeInMillis(dateInMillis);
        int julianDay = calendar.get(Calendar.DAY_OF_YEAR);

        if (currentJulianDay == julianDay) {  //today
            return context.getString(R.string.today);


        } else if(julianDay == currentJulianDay +1) {  //tomorrow
            return context.getString(R.string.tomorrow);

        } else {                   /// after tmr
            return getformattedDate(dateInMillis, "EEE");


        }

    }

    public static String getFormattedWind(Context context, double windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
            windSpeed = 3.6d * windSpeed;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = 2.23694d * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
//        String direction = "Unknown";
//        if (degrees >= 337.5 || degrees < 22.5) {
//            direction = "N";
//        } else if (degrees >= 22.5 && degrees < 67.5) {
//            direction = "NE";
//        } else if (degrees >= 67.5 && degrees < 112.5) {
//            direction = "E";

        //user better way:
        String[] directions = new String[] {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        String direction = directions[(int) Math.round(degrees / 45) % 8];
        return String.format(context.getString(windFormat), windSpeed, direction) + " (" + degrees + " deg)";
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        //http://openweathermap.org/weather-conditions

        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
         return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
         return R.drawable.ic_rain;
         } else if (weatherId == 511) {
         return R.drawable.ic_snow;
         } else if (weatherId >= 520 && weatherId <= 531) {
          return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
         return R.drawable.ic_snow;
         } else if (weatherId >= 701 && weatherId <= 761) {
         return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
          return R.drawable.ic_storm;
        } else if (weatherId == 800) {
          return R.drawable.ic_clear;
         } else if (weatherId == 801) {
           return R.drawable.ic_light_clouds;
         } else if (weatherId >= 802 && weatherId <= 804) {
         return R.drawable.ic_cloudy;
        }
    return -1;
    }

    /**
     +     * Helper method to provide the art resource id according to the weather condition id returned
     +     * by the OpenWeatherMap call.
     +     * @param weatherId from OpenWeatherMap API response
     +     * @return resource id for the corresponding icon. -1 if no relation is found.
     +     */
     public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
         //http://openweathermap.org/weather-conditions
         if (weatherId >= 200 && weatherId <= 232) {
             return R.drawable.art_storm;
         } else if (weatherId >= 300 && weatherId <= 321) {
             return R.drawable.art_light_rain;
         } else if (weatherId >= 500 && weatherId <= 504) {
             return R.drawable.art_rain;
         } else if (weatherId == 511) {
             return R.drawable.art_snow;
         } else if (weatherId >= 520 && weatherId <= 531) {
             return R.drawable.art_rain;
         } else if (weatherId >= 600 && weatherId <= 622) {
             return R.drawable.art_snow;
         } else if (weatherId >= 701 && weatherId <= 761) {
             return R.drawable.art_fog;
         } else if (weatherId == 761 || weatherId == 781) {
             return R.drawable.art_storm;
         } else if (weatherId == 800) {
             return R.drawable.art_clear;
         } else if (weatherId == 801) {
             return R.drawable.art_light_clouds;
         } else if (weatherId >= 802 && weatherId <= 804) {
             return R.drawable.art_clouds;
         }

         return -1;
    }
}
