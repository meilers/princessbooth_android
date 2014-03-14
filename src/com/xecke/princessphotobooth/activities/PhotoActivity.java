package com.xecke.princessphotobooth.activities;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.xecke.princessphotobooth.R;
import com.xecke.princessphotobooth.util.AnalyticsUtil;
/**
 * Activity displaying the taken photo and offering to share it with other apps.
 *
 */
public class PhotoActivity extends BaseFragmentActivity {
	
	private static final String TAG = PhotoActivity.class.getSimpleName();
	
    private static final String MIME_TYPE = "image/jpeg";
 
    private Uri uri;
    private ImageView mPhotoView;
    
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        
        uri = getIntent().getData();

        setContentView(R.layout.activity_photo);
        setTitle("Princess Booth");
        
		
        mPhotoView = (ImageView) findViewById(R.id.photo); 
        mPhotoView.setImageURI(uri);
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	AnalyticsUtil.sendView(this, TAG);
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.activity_photo, menu);

      initializeShareAction(menu.findItem(R.id.share));

      return super.onCreateOptionsMenu(menu);
    }
    
    
    private void initializeShareAction(MenuItem shareItem) {
        ShareActionProvider shareProvider = (ShareActionProvider) shareItem.getActionProvider();
        
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType(MIME_TYPE);

        shareProvider.setShareIntent(shareIntent);
        
        shareProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                String shareTarget = intent.getComponent().getPackageName();
                
                AnalyticsUtil.sendAction(PhotoActivity.this, AnalyticsUtil.EventCategories.PHOTO, AnalyticsUtil.EventActions.SHARE_PICTURE, shareTarget, null);
                return false;
            }
        });
    }
    
}
