package com.xecke.princessphotobooth.managers;

import com.xecke.princessphotobooth.application.PrincessPhotoboothApplication;

import android.content.res.AssetManager;
import android.graphics.Typeface;


public enum FontManager {

	INSTANCE;
	
	private Typeface mAppFont;
	
	private FontManager(){
		AssetManager assetManager = PrincessPhotoboothApplication.getContext().getResources().getAssets();
		 mAppFont = Typeface.createFromAsset(assetManager, "fonts/Storybook.ttf");
		 
	}
	
	public Typeface getAppFont(){
		return mAppFont;   
	}
	
}
