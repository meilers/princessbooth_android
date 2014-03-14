package com.xecke.princessphotobooth.ui;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.xecke.princessphotobooth.R;
import com.xecke.princessphotobooth.controllers.MultiTouchController;
import com.xecke.princessphotobooth.controllers.MultiTouchController.MultiTouchObjectCanvas;
import com.xecke.princessphotobooth.controllers.MultiTouchController.PointInfo;
import com.xecke.princessphotobooth.controllers.MultiTouchController.PositionAndScale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.YuvImage;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements MultiTouchObjectCanvas<CameraPreview.Img>  {

	
	
	
    public CameraPreview(Context context) {
        super(context);
        
        
        init(context);
    }
    private static final double ASPECT_RATIO = 3.0 / 4.0;

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (width > height * ASPECT_RATIO) {
             width = (int) (height * ASPECT_RATIO + .5);
        } else {
            height = (int) (width / ASPECT_RATIO + .5);
        }

        setMeasuredDimension(width, height);
    }
    
	
    
	// TODO: add drawables here
    // They come from the res/drawable-?dpi folders
	private static final int[] IMAGES = { R.drawable.dress, R.drawable.tiara, R.drawable.mirror, R.drawable.wand, R.drawable.necklace, R.drawable.shoe, R.drawable.frog, R.drawable.castle, R.drawable.trolley, R.drawable.cloud, R.drawable.perfume };

	private ArrayList<Img> mImages = new ArrayList<Img>(); 

	// --

	private MultiTouchController<Img> multiTouchController = new MultiTouchController<Img>(this);

	// --

	private PointInfo currTouchPoint = new PointInfo();

	private boolean mShowDebugInfo = true;

	private static final int UI_MODE_ROTATE = 1, UI_MODE_ANISOTROPIC_SCALE = 2;

	private int mUIMode = UI_MODE_ROTATE;

	// --

	private Paint mLinePaintTouchPointCircle = new Paint();
	
	private float mLastCanvasWidth;
	private float mLastCanvasHeight;
	
	public class Img {
		
		private int resId;
		public boolean mHidden = true;
		public boolean mDontMove = false;
		public boolean mSaving = false;
		
		private Drawable drawable;
		private Rect bounds;

		private boolean firstLoad;

		private int width, height, displayWidth, displayHeight;

		private float centerX, centerY, scaleX, scaleY, angle;

		private float minX, maxX, minY, maxY;

		private static final float SCREEN_MARGIN = 100;

		public Img(int resId, Resources res) {
			this.resId = resId;
			this.firstLoad = true;
			getMetrics(res);
		}

		private void getMetrics(Resources res) {
			DisplayMetrics metrics = res.getDisplayMetrics();
			// The DisplayMetrics don't seem to always be updated on screen rotate, so we hard code a portrait
			// screen orientation for the non-rotated screen here...
			// this.displayWidth = metrics.widthPixels;
			// this.displayHeight = metrics.heightPixels;
			this.displayWidth = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math.max(metrics.widthPixels,
					metrics.heightPixels) : Math.min(metrics.widthPixels, metrics.heightPixels);
			this.displayHeight = res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? Math.min(metrics.widthPixels,
					metrics.heightPixels) : Math.max(metrics.widthPixels, metrics.heightPixels);
		}

		/** Called by activity's onResume() method to load the images */
		public void load(Resources res) {
			getMetrics(res);
			this.drawable = res.getDrawable(resId);
			this.width = drawable.getIntrinsicWidth();
			this.height = drawable.getIntrinsicHeight();
			float cx, cy, sx, sy;
			if (firstLoad) {
				cx = displayWidth/2;
				cy = displayHeight/2-200;
				float sc = 2.0f;
				sx = sy = sc;
				firstLoad = false;
			} else {
				// Reuse position and scale information if it is available
				// FIXME this doesn't actually work because the whole activity is torn down and re-created on rotate
				cx = this.centerX;
				cy = this.centerY;
				sx = this.scaleX;
				sy = this.scaleY;
				// Make sure the image is not off the screen after a screen rotation
				if (this.maxX < SCREEN_MARGIN)
					cx = SCREEN_MARGIN;
				else if (this.minX > displayWidth - SCREEN_MARGIN)
					cx = displayWidth - SCREEN_MARGIN;
				if (this.maxY > SCREEN_MARGIN)
					cy = SCREEN_MARGIN;
				else if (this.minY > displayHeight - SCREEN_MARGIN)
					cy = displayHeight - SCREEN_MARGIN;
			}
			setPos(cx, cy, sx, sy, 0.0f);
		}

		/** Called by activity's onPause() method to free memory used for loading the images */
		public void unload() {
			this.drawable = null;
		}

		/** Set the position and scale of an image in screen coordinates */
		public boolean setPos(PositionAndScale newImgPosAndScale) {
			return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(), (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale
					.getScaleX() : newImgPosAndScale.getScale(), (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale.getScaleY()
					: newImgPosAndScale.getScale(), newImgPosAndScale.getAngle());
			// FIXME: anisotropic scaling jumps when axis-snapping
			// FIXME: affine-ize
			// return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(), newImgPosAndScale.getScaleAnisotropicX(),
			// newImgPosAndScale.getScaleAnisotropicY(), 0.0f);
		}

		
		/** Set the position and scale of an image in screen coordinates */
		public boolean setPos(float centerX, float centerY, float scaleX, float scaleY, float angle) {
			
			if( !mDontMove )
			{
				float ws = (width / 2) * scaleX, hs = (height / 2) * scaleY;
				float newMinX = centerX - ws, newMinY = centerY - hs, newMaxX = centerX + ws, newMaxY = centerY + hs;
				if ( (newMinX > displayWidth - SCREEN_MARGIN || newMaxX < SCREEN_MARGIN || newMinY > displayHeight - SCREEN_MARGIN
						|| newMaxY < SCREEN_MARGIN ) && !mSaving )
					return false;
				this.centerX = centerX;
				this.centerY = centerY;
				this.scaleX = scaleX;
				this.scaleY = scaleY;
				this.angle = angle;
				this.minX = newMinX;
				this.minY = newMinY;
				this.maxX = newMaxX;
				this.maxY = newMaxY;
			}
			return true;
		}

		/** Return whether or not the given screen coords are inside this image */
		public boolean containsPoint(float scrnX, float scrnY) {
			// FIXME: need to correctly account for image rotation
			return (scrnX >= minX && scrnX <= maxX && scrnY >= minY && scrnY <= maxY);
		}

		public void draw(Canvas canvas) {
			canvas.save();
			float dx = (maxX + minX) / 2;
			float dy = (maxY + minY) / 2;
			drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
			canvas.translate(dx, dy);
			canvas.rotate(angle * 180.0f / (float) Math.PI);
			canvas.translate(-dx, -dy);
			drawable.draw(canvas);
			canvas.restore();
			
			Log.d("c height", canvas.getHeight()+"");
			Log.d("c width", canvas.getWidth()+"");
			
			bounds = drawable.getBounds(); // store this for pic
		}
		

		
		//me
		public void drawForPic(Canvas canvas) {
			drawable.setBounds(bounds);
			drawable.draw(canvas);
		}
		
		public int getResId()
		{
			return resId;
		}
		

		public Drawable getDrawable() {
			return drawable;
		}

		public int getWidth() {
			return width;
		}
		
		public void setWidth(int w ) {
			width = w;
		}

		public int getHeight() {
			return height;
		}
		
		public void setHeight(int h ) {
			height = h;
		}


		public float getCenterX() {
			return centerX;
		}

		public float getCenterY() {
			return centerY;
		}

		public float getScaleX() {
			return scaleX;
		}

		public float getScaleY() {
			return scaleY;
		}

		public float getAngle() {
			return angle;
		}

		// FIXME: these need to be updated for rotation
		public float getMinX() {
			return minX;
		}

		public float getMaxX() {
			return maxX;
		}

		public float getMinY() {
			return minY;
		}

		public float getMaxY() {
			return maxY;
		}
	}

 

	private void init(Context context) {
        setWillNotDraw(false);
        
        
		Resources res = context.getResources();
		for (int i = 0; i < IMAGES.length; i++)
			mImages.add(new Img(IMAGES[i], res));
		
		mLinePaintTouchPointCircle.setColor(Color.YELLOW);
		mLinePaintTouchPointCircle.setStrokeWidth(5);
		mLinePaintTouchPointCircle.setStyle(Style.STROKE);
		mLinePaintTouchPointCircle.setAntiAlias(true);
//		setBackgroundColor(Color.BLACK);
	}
	
	public boolean isImageHidden( int resId )
	{
		int pos = -1;
		
		for(int i =0; i < mImages.size(); ++i )
		{
			if( mImages.get(i).getResId() == resId)
			{
				pos = i;
			}
		}
			
		
		if( pos != -1 )
		{
			return mImages.get(pos).mHidden;
		}
		else
			return false;
	}
	
	public void addImage( int resId )
	{
		int pos = -1;
		
		for(int i =0; i < mImages.size(); ++i )
		{
			if( mImages.get(i).getResId() == resId)
			{
				pos = i;
			}
		}
		
		if( pos != -1 )
		{
			mImages.get(pos).mHidden = false;
		}
	
		invalidate();
	}
	
	public void removeImage( int resId )
	{
		int pos = -1;
		
		for(int i =0; i < mImages.size(); ++i )
		{
			if( mImages.get(i).getResId() == resId)
			{
				pos = i;
			}
		}
		
		if( pos != -1 )
		{
			mImages.get(pos).mHidden = true;
		}
		
		invalidate();
	}
	
	

	/** Called by activity's onResume() method to load the images */
	public void loadImages(Context context) {
		Resources res = context.getResources();
		int n = mImages.size();
		for (int i = 0; i < n; i++)
			mImages.get(i).load(res);
	}

	/** Called by activity's onPause() method to free memory used for loading the images */
	public void unloadImages() {
		int n = mImages.size();
		for (int i = 0; i < n; i++)
			mImages.get(i).unload();
	}
	
	public ArrayList<Img> getImages()
	{
		return mImages;
	}

	// ---------------------------------------------------------------------------------------------------

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int n = mImages.size();
		for (int i = 0; i < n; i++)
		{
			if( !mImages.get(i).mHidden )
				mImages.get(i).draw(canvas);
		}
		if (mShowDebugInfo)
			drawMultitouchDebugMarks(canvas);
		
		
		mLastCanvasHeight = canvas.getHeight();
		mLastCanvasWidth = canvas.getWidth();
	}

	public float getLastCanvasHeight() {
		return mLastCanvasHeight;
	}

	public float getLastCanvasWidth() {
		return mLastCanvasWidth;
	}
	
	// ---------------------------------------------------------------------------------------------------

	public void trackballClicked() {
		mUIMode = (mUIMode + 1) % 3;
		invalidate();
	}

	private void drawMultitouchDebugMarks(Canvas canvas) {
		if (currTouchPoint.isDown()) {
			float[] xs = currTouchPoint.getXs();
			float[] ys = currTouchPoint.getYs();
			float[] pressures = currTouchPoint.getPressures();
			int numPoints = Math.min(currTouchPoint.getNumTouchPoints(), 2);
			for (int i = 0; i < numPoints; i++)
				canvas.drawCircle(xs[i], ys[i], 50 + pressures[i] * 80, mLinePaintTouchPointCircle);
			if (numPoints == 2)
				canvas.drawLine(xs[0], ys[0], xs[1], ys[1], mLinePaintTouchPointCircle);
		}
	}

	// ---------------------------------------------------------------------------------------------------

	/** Pass touch events to the MT controller */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return multiTouchController.onTouchEvent(event);
	}

	/** Get the image that is under the single-touch point, or return null (canceling the drag op) if none */
	public Img getDraggableObjectAtPoint(PointInfo pt) {
		float x = pt.getX(), y = pt.getY();
		int n = mImages.size();
		for (int i = n - 1; i >= 0; i--) {
			Img im = mImages.get(i);
			if (im.containsPoint(x, y) && !im.mHidden)
				return im;
		}
		return null;
	}

	/**
	 * Select an object for dragging. Called whenever an object is found to be under the point (non-null is returned by getDraggableObjectAtPoint())
	 * and a drag operation is starting. Called with null when drag op ends.
	 */
	public void selectObject(Img img, PointInfo touchPoint) {
		currTouchPoint.set(touchPoint);
		if (img != null) {
			// Move image to the top of the stack when selected
			mImages.remove(img);
			mImages.add(img);
		} else {
			// Called with img == null when drag stops.
		}
		invalidate();
	}

	/** Get the current position and scale of the selected image. Called whenever a drag starts or is reset. */
	public void getPositionAndScale(Img img, PositionAndScale objPosAndScaleOut) {
		// FIXME affine-izem (and fix the fact that the anisotropic_scale part requires averaging the two scale factors)
		objPosAndScaleOut.set(img.getCenterX(), img.getCenterY(), (mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
				(img.getScaleX() + img.getScaleY()) / 2, (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, img.getScaleX(), img.getScaleY(),
				(mUIMode & UI_MODE_ROTATE) != 0, img.getAngle());
	}

	/** Set the position and scale of the dragged/stretched image. */
	public boolean setPositionAndScale(Img img, PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
		currTouchPoint.set(touchPoint);
		boolean ok = img.setPos(newImgPosAndScale);
		if (ok)
			invalidate();
		return ok;
	}

	Bitmap mBmp;

	
	public Bitmap getBmp()
	{
		return mBmp;
	}
}
