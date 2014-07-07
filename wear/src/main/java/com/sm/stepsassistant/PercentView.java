package com.sm.stepsassistant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PercentView extends View {

    public static float percent;
    private Paint paint;
    private Paint bgpaint;
    private RectF rect;

    private double screenRatio = 0.75;
    private float lineWidth = 14;

    public PercentView(Context context){
        super(context);
        init();
    }

    public PercentView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public PercentView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init(){
        paint = new Paint();
        paint.setColor(getContext().getResources().getColor(R.color.play_blue_light));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        bgpaint = new Paint();
        bgpaint.setColor(getContext().getResources().getColor(R.color.black));
        bgpaint.setAntiAlias(true);
        bgpaint.setStyle(Paint.Style.FILL);
        rect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = (int) (getHeight()*screenRatio);
        int width = (int) (getWidth()*screenRatio);
        int left = (getWidth()-width)/2;
        int top = (getHeight()-height)/2;
        rect.set(left, top, left + width, top + width);
        if (percent != 0) {
            canvas.drawArc(rect, -90, (360 * percent), false, paint);
        }
    }
}