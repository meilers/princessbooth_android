package com.xecke.princessphotobooth.application;

import java.util.Collection;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

public class PrincessPhotoboothApplication extends Application {

	private static Context sContext;
	
	
    
	@Override
	public void onCreate() {
		super.onCreate();

		sContext = getApplicationContext();
		
	}

	
	public static final Context getContext() {
		return sContext;
	}

}
