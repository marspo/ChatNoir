package com.example.martinjonovski.chatnoir;

import android.app.Application;

/**
 * Created by Martin Jonovski on 10/25/2017.
 */

public class TimeAgo extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MIN_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MIN_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long mllis) {

        long now = System.currentTimeMillis();
        if (mllis > now || mllis <= 0) {
            return null;
        }
        final long diff = now - mllis;

        if (diff < MIN_MILLIS) {
            return "just now";
        } else if (diff < 2 * MIN_MILLIS) {
            return "1 minute ago";

        } else if (diff < 50 * MIN_MILLIS) {
            return diff / MIN_MILLIS + " mins ago";
        } else if (diff < 90 * HOUR_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
