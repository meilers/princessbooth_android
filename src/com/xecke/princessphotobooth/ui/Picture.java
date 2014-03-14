package com.xecke.princessphotobooth.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

/** Class that is responsible to move the picture on the SurfaceView */
public class Picture {
	
	private final ImageView imageView;
	private LayoutParams layoutParams;
	
	private final int mRessourceId;
	
	 
	public Picture(ImageView imageView, int res,  Context context) {
		this.imageView = imageView;
		mRessourceId = res;
	}
	
	public int getWidth() {
		return layoutParams.width;
	}

	public int getHeight() {
		return layoutParams.height;
	}
	
	public int getLeft(){
		return imageView.getLeft();
	}
	
	public int getTop(){
		return imageView.getTop();
	}
	
	public int getRight(){
		return imageView.getRight();
	}
	
	public int getBottom(){
		return imageView.getBottom();
	}
	
	public ImageView getImageView() {
		return imageView;
	}
	
	public void setVisibility(int gone) {
		imageView.setVisibility(gone);
	}

	public int getVisibility() {
		return imageView.getVisibility();
	}
	
	public Drawable getDrawable() {
		return imageView.getDrawable();
	}
	
	public int getRessourceId() {
		return mRessourceId;
	}

	public void setOnTouchListener(final int windowWidth, final int windowHeight) {
		imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                  layoutParams = (LayoutParams) imageView
                                .getLayoutParams();
                  switch (event.getAction()) {
                  case MotionEvent.ACTION_MOVE:
                         int x_cord = (int) event.getRawX();
                         int y_cord = (int) event.getRawY();

                         if (x_cord > windowWidth) {
                                x_cord = windowWidth;
                         }
                         if (y_cord > windowHeight) {
                                y_cord = windowHeight;
                         }

                         layoutParams.width = x_cord - 25;
                         layoutParams.height = y_cord - 75;

                         imageView.setLayoutParams(layoutParams);
                         //Toast.makeText(context, "layoutParams width: " + layoutParams.width + " , layoutParams height: " + layoutParams.height, Toast.LENGTH_SHORT).show();
                         break;
                  default:
                         break;
                  }
                  return true;
            }
     });	
	}


	public LayoutParams getLayoutParams() {
		return imageView.getLayoutParams();
	}

	public void setLayoutParams(LayoutParams layoutParams) {
		imageView.setLayoutParams(layoutParams);
	}
	

}
