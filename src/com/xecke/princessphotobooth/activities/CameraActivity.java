package com.xecke.princessphotobooth.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.xecke.princessphotobooth.R;
import com.xecke.princessphotobooth.fragments.CameraFragment;
import com.xecke.princessphotobooth.listeners.CameraFragmentListener;
import com.xecke.princessphotobooth.ui.Picture;
import com.xecke.princessphotobooth.util.AnalyticsUtil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * Activity displaying the camera and mustache preview.
 *
 */
public class CameraActivity extends BaseFragmentActivity implements CameraFragmentListener {

	private static final String TAG = CameraActivity.class.getSimpleName();
	
	private Picture mDressPic, mWandPic, mMirrorPic,
			mNecklacePic, mTiaraPic, mShoePic,
			mFrogPic, mCastlePic, mPerfumePic,
			mCloudPic, mTrolleyPic;

	private int windowWidth;
	private int windowHeight;
	
	
	final static String DEBUG_TAG = "PhotoBoothActivity";
	private LayoutInflater controlInflater = null;
	private View viewControl;
	
	private CameraFragment mCameraFragment;
	

    private static final int PICTURE_QUALITY = 90;

    /**
     * On activity getting created.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setTitle("Princess Booth");
        

		// Get the size of the screen
		windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		windowHeight = getWindowManager().getDefaultDisplay().getHeight();

		controlInflater = LayoutInflater.from(getBaseContext());
		// Add the capture button and the center image
		viewControl = controlInflater.inflate(R.layout.control, null);
		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(viewControl, layoutParamsControl);
		// Add the info and close buttons
		viewControl = controlInflater.inflate(R.layout.info_close, null);
		this.addContentView(viewControl, layoutParamsControl);




		if (checkCameraHardware(getApplicationContext())) {

			// Set the different buttons and their listeners on the screen
			setWidgets();

		} else {
			Toast.makeText(getApplicationContext(),
					"No camera found on this device", Toast.LENGTH_LONG).show();
		}
		
		mCameraFragment = (CameraFragment) getFragmentManager().findFragmentById(
	            R.id.camera_fragment
		        );

    }

    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	AnalyticsUtil.sendView(this, TAG);
    }
    
    
    /**
     * On fragment notifying about a non-recoverable problem with the camera.
     */
    @Override
    public void onCameraError() {
        Toast.makeText(
            this,
            "camera error",
            Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    /**
     * The user wants to take a picture.
     *
     * @param view
     */
    public void takePicture(View view) {
    	String label = "";
    	
		mMirrorPic.setVisibility(View.GONE);
		mNecklacePic.setVisibility(View.GONE);
		mTiaraPic.setVisibility(View.GONE);
		mShoePic.setVisibility(View.GONE);
		
		mPerfumePic.setVisibility(View.GONE);
		
		mFrogPic.setVisibility(View.GONE);
		mCastlePic.setVisibility(View.GONE);
		mCloudPic.setVisibility(View.GONE);
		mTrolleyPic.setVisibility(View.GONE);
		
    	if( mDressPic.getVisibility() == 0 )
    		label += " dress, ";
    	
    	if( mWandPic.getVisibility() == 0 )
    		label += " wand, ";
    	
    	if( mMirrorPic.getVisibility() == 0 )
    		label += " mirror, ";
    	
    	if( mNecklacePic.getVisibility() == 0 )
    		label += " necklace, ";
    	
    	if( mTiaraPic.getVisibility() == 0 )
    		label += " label, ";
    	
    	if( mShoePic.getVisibility() == 0 )
    		label += " shoe, ";
    	
    	if( mPerfumePic.getVisibility() == 0 )
    		label += " perfume, ";
    	
    	if( mFrogPic.getVisibility() == 0 )
    		label += " frog, ";
    	
    	if( mCastlePic.getVisibility() == 0 )
    		label += " castle, ";
    	
    	if( mCloudPic.getVisibility() == 0 )
    		label += " cloud, ";
    	
    	if( mTrolleyPic.getVisibility() == 0 )
    		label += " trolley, ";
    	
    	AnalyticsUtil.sendAction(this, AnalyticsUtil.EventCategories.PHOTO, AnalyticsUtil.EventActions.TAKE_PICTURE, label, null);
    	
        view.setEnabled(false);

        mCameraFragment.takePicture();
    }

    /**
     * A picture has been taken.
     */
    public void onPictureTaken(Bitmap bitmap) {
        File mediaStorageDir = new File(
            Environment.getExternalStoragePublicDirectory( 
                Environment.DIRECTORY_PICTURES
            ),
            getString(R.string.app_name)
        );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                showSavingPictureErrorToast();
                return;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(
            mediaStorageDir.getPath() + File.separator + "Princess Booth"+ timeStamp + ".jpg"
        );

        try {
            FileOutputStream stream = new FileOutputStream(mediaFile);
            bitmap.compress(CompressFormat.JPEG, PICTURE_QUALITY, stream);
        } catch (IOException exception) {
            showSavingPictureErrorToast();

            Log.w(TAG, "IOException during saving bitmap", exception);
            return;
        }

        MediaScannerConnection.scanFile(
            this,
            new String[] { mediaFile.toString() },
            new String[] { "image/jpeg" },
            null
        );

        Intent intent = new Intent(this, PhotoActivity.class);
        intent.setData(Uri.fromFile(mediaFile));
        startActivity(intent);
        
        finish();
    }

    private void showSavingPictureErrorToast() {
        Toast.makeText(this, getText(R.string.toast_error_save_picture), Toast.LENGTH_SHORT).show();
    }
    
    
    
    
    
    
    

	/** Set the different buttons and their listeners on the screen */
	private void setWidgets() {
		// Load the different pictures on the screen
		loadPictures();
		// Set the visibility to GONE to the rest of the ImageViews and
		// ImageButtons
		mDressPic.setVisibility(View.GONE);
		mWandPic.setVisibility(View.GONE);
		mMirrorPic.setVisibility(View.GONE);
		mNecklacePic.setVisibility(View.GONE);
		mTiaraPic.setVisibility(View.GONE);
		mShoePic.setVisibility(View.GONE);
		
		mPerfumePic.setVisibility(View.GONE);
		
		mFrogPic.setVisibility(View.GONE);
		mCastlePic.setVisibility(View.GONE);
		mCloudPic.setVisibility(View.GONE);
		mTrolleyPic.setVisibility(View.GONE);
		


		
		// Add a listener to the capture button
//		captureButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// Show the composition image
//				final Dialog settingsDialog = new Dialog(CameraActivity.this,
//						android.R.style.Theme_Translucent_NoTitleBar);
//				settingsDialog.getWindow().requestFeature(
//						Window.FEATURE_NO_TITLE);
//				settingsDialog.setContentView(CameraActivity.this.getLayoutInflater().inflate(
//						R.layout.composition, null));
//				settingsDialog.show();
//				
//				takePicture(mCameraFragment.getCameraPreview());
//				
//			}
//		});



		// TODO: add ids here. They come from the activity_camera.xml file in the res/layout folder
		mDressPic.setVisibility(View.GONE);
		mWandPic.setVisibility(View.GONE);
		mMirrorPic.setVisibility(View.GONE);
		mNecklacePic.setVisibility(View.GONE);
		mTiaraPic.setVisibility(View.GONE);
		mShoePic.setVisibility(View.GONE);
		
		mPerfumePic.setVisibility(View.GONE);
		
		mFrogPic.setVisibility(View.GONE);
		mCastlePic.setVisibility(View.GONE);
		mCloudPic.setVisibility(View.GONE);
		mTrolleyPic.setVisibility(View.GONE);
		
		
		ImageButton dressIb = (ImageButton) findViewById(R.id.dress_btn);
		ImageButton wandIb = (ImageButton) findViewById(R.id.wand_btn);
		ImageButton mirrorIb = (ImageButton) findViewById(R.id.mirror_btn);
		ImageButton necklaceIb = (ImageButton) findViewById(R.id.necklace_btn);
		ImageButton tiaraBtn = (ImageButton) findViewById(R.id.tiara_btn);
		ImageButton shoeBtn = (ImageButton) findViewById(R.id.shoe_btn);
		ImageButton perfumeBtn = (ImageButton) findViewById(R.id.perfume_btn);
		ImageButton frogBtn = (ImageButton) findViewById(R.id.frog_btn);
		ImageButton castleBtn = (ImageButton) findViewById(R.id.castle_btn);
		ImageButton cloudBtn = (ImageButton) findViewById(R.id.cloud_btn);
		ImageButton trolleyBtn = (ImageButton) findViewById(R.id.trolley_btn);
		
		// TODO: dont forget the listeners
		dressIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mDressPic);
			}
		});

		wandIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mWandPic);
			}
		});

		mirrorIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mMirrorPic);
			}
		});

		necklaceIb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mNecklacePic);
			}
		});

		tiaraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mTiaraPic);
			}
		});

		shoeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mShoePic);
			}
		});
		
		perfumeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mPerfumePic);
			}
		});

		frogBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mFrogPic);
			}
		});

		castleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mCastlePic);
			}
		});

		cloudBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mCloudPic);
			}
		});

		trolleyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility(mTrolleyPic);
			}
		});

		

		// Add the different listeners on the top pictures
		mWandPic.setOnTouchListener(windowWidth, windowHeight);
		mMirrorPic.setOnTouchListener(windowWidth, windowHeight);
		mDressPic.setOnTouchListener(windowWidth, windowHeight);
		mNecklacePic.setOnTouchListener(windowWidth, windowHeight);
		mTiaraPic.setOnTouchListener(windowWidth, windowHeight);
		mShoePic.setOnTouchListener(windowWidth, windowHeight);
		mPerfumePic.setOnTouchListener(windowWidth, windowHeight);
		mFrogPic.setOnTouchListener(windowWidth, windowHeight);
		mCastlePic.setOnTouchListener(windowWidth, windowHeight);
		mCloudPic.setOnTouchListener(windowWidth, windowHeight);
		mTrolleyPic.setOnTouchListener(windowWidth, windowHeight);
	}

	

	
	/** Load the different images on the screen */
	// TODO: add ids and drawables here. They come from the res/drawable-?dpi folder and res/layout folder
	
	private void loadPictures() {
		mDressPic = new Picture(
				(ImageView) findViewById(R.id.dress_iv), R.drawable.dress, this);
		mWandPic = new Picture((ImageView) findViewById(R.id.wand_iv), R.drawable.wand, this);
		mMirrorPic = new Picture((ImageView) findViewById(R.id.mirror_iv), R.drawable.mirror,
				this);
		mNecklacePic = new Picture(
				(ImageView) findViewById(R.id.necklace_iv), R.drawable.necklace, this);
		mTiaraPic = new Picture(
				(ImageView) findViewById(R.id.tiara_iv), R.drawable.tiara, this);
		mShoePic = new Picture(
				(ImageView) findViewById(R.id.shoe_iv), R.drawable.shoe, this);
		
		mPerfumePic = new Picture((ImageView) findViewById(R.id.perfume_iv), R.drawable.perfume, this);
		mFrogPic = new Picture((ImageView) findViewById(R.id.frog_iv), R.drawable.frog,
				this);
		mCastlePic = new Picture(
				(ImageView) findViewById(R.id.castle_iv), R.drawable.castle, this);
		mCloudPic = new Picture(
				(ImageView) findViewById(R.id.cloud_iv), R.drawable.cloud, this);
		mTrolleyPic = new Picture(
				(ImageView) findViewById(R.id.trolley_iv), R.drawable.trolley, this);
	}


	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// This device has a camera
			return true;
		} else {
			// No camera on this device
			return false;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // Attempt to get a Camera instance
			c.setPreviewCallback/*WithBuffer*/(new PreviewCallback() {

				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					System.out.println("callback called");

				}
			});

			c.startPreview();
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			e.printStackTrace();
		}
		return c; // Returns null if camera is unavailable
	}

	@Override
	protected void onResume() {
		super.onResume();
		

		mCameraFragment.getCameraPreview().loadImages(this);
	}
	
	@Override
	protected void onPause() {
		mCameraFragment.getCameraPreview().unloadImages();
		
		super.onPause();
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	Handler mHideHandler = new Handler();


	/** Check which image is visible and set the new image visible */
	private void setVisibility(Picture picture) {
		if(  mCameraFragment.getCameraPreview().isImageHidden(picture.getRessourceId()))
			mCameraFragment.getCameraPreview().addImage(picture.getRessourceId());
		else
			mCameraFragment.getCameraPreview().removeImage(picture.getRessourceId());
	}
	
	public void setImageView(ImageView v)
	{
		setContentView(v);
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.activity_camera, menu);

      return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	switch( item.getItemId() )
    	{
    	case R.id.capture:
			
			takePicture(mCameraFragment.getCameraPreview());
    		break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }

}
