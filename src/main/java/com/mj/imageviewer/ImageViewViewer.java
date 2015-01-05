/**
 *
 * copyright(C)2014- 
 *
 */
package com.mj.imageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * @author jixiaolong<microjixl@gmail.com>
 * @code: create：15-1-5下午12:52
 */
public class ImageViewViewer extends ImageView{
    private final float MAX_SCALE = 4;
    private final float MIN_SCALE = 0.5f;
    private int mtMode = 0;//Multi-Touch
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private PointF fingerStartPoint = new PointF();
    private PointF fingersCenterPoint = new PointF();
    private float lastDistance = 0f;
    private float totalScale = 1f;
    private float originalScale = 1f;
    private Matrix matrix;
    private RectF bitmapRectF = new RectF();
    private GestureDetector mDetector;

    public ImageViewViewer(Context context) {
        super(context);
        init();
    }

    public ImageViewViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Runnable clickRun = new Runnable() {
        @Override
        public void run() {
            performClick();
        }
    };

    private void init(){
        setScaleType(ScaleType.FIT_CENTER);
        mDetector = new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                removeCallbacks(clickRun);
                RectF dst = new RectF();
                matrix.mapRect(dst, bitmapRectF);
                if(dst.contains(e.getX(), e.getY())){
                    float s = totalScale / originalScale;
                    if(s == 1){
                        //not original size. scale double multiple
                        matrix.postScale(MAX_SCALE*originalScale/2/totalScale, MAX_SCALE*originalScale/2/totalScale,e.getX(),e.getY());
                        totalScale = MAX_SCALE*originalScale/2;
                    }else{
                        //s<0 <
                        matrix.postScale(originalScale/totalScale, originalScale/totalScale,e.getX(),e.getY());
                        totalScale = originalScale;
                    }
                    invalidate();
                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                postDelayed(clickRun,300);
                return false;
            }
        });
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        matrix = null;
    }
    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        bitmapRectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix = null;
    }
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        matrix = null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(matrix == null){
            matrix = new Matrix();
            matrix.set(getImageMatrix());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return multiTouchEvent(event);
    }

    /**
     * dispatch multiTouchEvent.
     * @param event
     * @return
     */
    private boolean multiTouchEvent(MotionEvent event){
        if(matrix == null)
            return false;
        if(mDetector.onTouchEvent(event))
            return true;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                setScaleType(ScaleType.MATRIX);
                fingerStartPoint.set(event.getX(), event.getY());
                mtMode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mtMode == DRAG){
                    float dx = event.getX() - fingerStartPoint.x;
                    float dy = event.getY() - fingerStartPoint.y;
                    matrix.postTranslate(dx, dy);
                    fingerStartPoint.set(event.getX(), event.getY());
                }else if(mtMode == ZOOM){
                    float dis = distanceBetweenFingers(event);
                    if(dis > 5f){
                        RectF dst = new RectF();
                        matrix.mapRect(dst, bitmapRectF);
                        if(dst.contains(fingersCenterPoint.x,fingersCenterPoint.y)){
                            float scale = dis / lastDistance ;
                            lastDistance = dis;
                            float finalScale = totalScale * scale;
                            if(finalScale <= MAX_SCALE*1.8f && finalScale > MIN_SCALE*0.6f){
                                matrix.postScale(scale, scale,fingersCenterPoint.x,fingersCenterPoint.y);
                                totalScale = finalScale;
                            }
                        }
                    }
                }
                setImageMatrix(matrix);
                break;
            case MotionEvent.ACTION_UP:
                RectF dst = new RectF();
                matrix.mapRect(dst, bitmapRectF);
                int width = getWidth();
                int height = getHeight();

                if(dst.width() <= width){
                    float translateX = width/2f - dst.centerX();
                    matrix.postTranslate(translateX, 0);
                }

                if(dst.height() <= height){
                    float translateY = height/2f - dst.centerY();
                    matrix.postTranslate(0, translateY);
                }

                if(dst.width() > width){
                    int padding = 0;
                    if(dst.left > padding){
                        //left blank
                        matrix.postTranslate(padding - dst.left, 0);
                    }else if(dst.right < (width - padding)){
                        //right blank
                        matrix.postTranslate(width - padding - dst.right, 0);
                    }
                }

                if(dst.height() > height){
                    int padding = 0;
                    if(dst.top > padding){
                        //top blank
                        matrix.postTranslate(0, padding - dst.top);
                    }else if(dst.bottom < (height - padding)){
                        //bottom blank
                        matrix.postTranslate(0, height - padding -dst.bottom);
                    }
                }
                setImageMatrix(matrix);
                mtMode = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mtMode = ZOOM;
                fingersCenterPoint = centerPointBetweenFingers(event);
                lastDistance = distanceBetweenFingers(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                float scale = 1f;
                if(totalScale > MAX_SCALE || totalScale < MIN_SCALE){
                    if(totalScale > MAX_SCALE){
                        scale = MAX_SCALE/totalScale;
                    }else{
                        scale = MIN_SCALE/totalScale;
                    }
                    matrix.postScale(scale, scale,fingersCenterPoint.x,fingersCenterPoint.y);
                    totalScale *= scale;
                }
                fingerStartPoint.set(event.getX(0), event.getY(0));
                setImageMatrix(matrix);
                mtMode = 0;
                break;
        }
        return true;
    }

    /**
     * calculate distance between fingers
     * @param event
     * @return
     */
    private float distanceBetweenFingers(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * calculate center point
     *
     * @param event
     */
    private PointF centerPointBetweenFingers(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float yPoint0 = event.getY(0);
        float xPoint1 = event.getX(1);
        float yPoint1 = event.getY(1);
        PointF p = new PointF();
        p.set((xPoint0 + xPoint1) / 2, (yPoint0 + yPoint1) / 2);
        return p;
    }
}
