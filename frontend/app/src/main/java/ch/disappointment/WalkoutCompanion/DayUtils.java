package ch.disappointment.WalkoutCompanion;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Returns the current date in the format "yyyy-MM-dd"
 */
public class DayUtils {
    public static String getCurrentDay(){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String date = jdf.format(new Date());
        return date.substring(0, 10);
    }
}
