package com.xecke.princessphotobooth.activities;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint; 
import android.text.style.MetricAffectingSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.analytics.tracking.android.EasyTracker;
import com.xecke.princessphotobooth.R;
import com.xecke.princessphotobooth.managers.FontManager;

public class BaseFragmentActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		
		
		
		
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setHomeButtonEnabled(false);
//		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar));
		
		
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	
	
	@Override 
	public void setTitle(CharSequence title) {
		String str = String.valueOf(title);
		str = str.toUpperCase(Locale.getDefault());
		SpannableString s = new SpannableString(str);
		MetricAffectingSpan span = new MetricAffectingSpan() {
			@Override
			public void updateMeasureState(TextPaint p) {
				p.setTypeface(FontManager.INSTANCE.getAppFont());
			}

			@Override
			public void updateDrawState(TextPaint tp) {
				tp.setTypeface(FontManager.INSTANCE.getAppFont());
			}
		};

		s.setSpan(span, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		getActionBar().setTitle(s);
	}
}
