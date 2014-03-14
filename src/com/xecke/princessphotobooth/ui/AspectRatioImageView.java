package com.xecke.princessphotobooth.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectRatioImageView extends ImageView {

    public AspectRatioImageView(Context context) {
        this(context, null, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	if( getDrawable() != null ) 
    	{
	        int width = MeasureSpec.getSize(widthMeasureSpec);
	        int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
	        setMeasuredDimension(width, height);
    	}
    	else
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
