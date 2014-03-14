package com.xecke.princessphotobooth.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class AnalyticsUtil {

	private static final class DimensionValues {
		private static final int LANGUAGE = 1;
		private static final int LOGGED_IN = 2;
		private static final int WAVE_ID = 3;
		private static final String EN = "en";
		private static final String PARTNER = "partner";
		private static final String MEMBER = "member";
		private static final String GUEST = "guest";
	}

	private static final class ShareValues {
		private static final String FACEBOOK = "Facebook";
		private static final String SHARE = "Share";
		private static final String TWITTER = "Twitter";
		private static final String TWEET = "Tweet";
		private static final String EMAIL = "Email";
		private static final String SEND = "Send";
	}

	public static final class EventCategories {
		public static final String PHOTO = "photo";

	}

	public static final class EventActions {
		public static final String TAKE_PICTURE = "take_picture";
		public static final String SHARE_PICTURE = "share_picture";
	}


	public static void sendView(Context c, String screenName) {
		Tracker easyTracker = EasyTracker.getInstance(c);

		easyTracker.set(Fields.SCREEN_NAME, screenName);

		easyTracker.send(MapBuilder.createAppView().build());
	}

	public static void sendAction(Context c, String category, String action, String label, Long value) {
		// May return null if a EasyTracker has not yet been initialized with a
		// property ID.
		EasyTracker easyTracker = EasyTracker.getInstance(c);

		// MapBuilder.createEvent().build() returns a Map of event fields and
		// values
		// that are set and sent with the hit.
		easyTracker.send(MapBuilder.createEvent(category, // Event category
																// (required)
				action, // Event action (required)
				label, // Event label
				value) // Event value
				.build());
	}
	
	

}
