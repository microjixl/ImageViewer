/**
 *
 * copyright(C)2014- 
 *
 */
package com.mj.imageviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * @author jixiaolong<microjixl@gmail.com>
 * @code: create：15-1-5下午12:52
 */
public class PictureViewActivity extends Activity{
    private ImageViewViewer show;
    private View lamp;
    private Animator mCurrentAnimator;
    private Rect startBounds;
    private int oritentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        startBounds = getIntent().getParcelableExtra("BOUND");
        oritentation = getIntent().getIntExtra("ORIENTATION",0);
        initView();
    }

    @Override
    public void onBackPressed() {
        show.performClick();
        return;
    }

    private void initView(){
        show = (ImageViewViewer)findViewById(R.id.iv_show);
        lamp = findViewById(R.id.v_lamp);
        show.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                zoomFromThumb(startBounds,(BitmapFactory.decodeResource(getResources(),R.drawable.image1)));
                show.removeOnLayoutChangeListener(this);
            }
        });

    }

    private void zoomFromThumb(final Rect startBounds,Bitmap image){
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        show.setImageBitmap(image);

        Rect finalBounds = new Rect();
        Point globalOffset = new Point();
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        show.setPivotX(0f);
        show.setPivotY(0f);
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(show, View.X,
                startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(show, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(show, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(show, View.SCALE_Y,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(lamp,"alpha",0.0f,1f));
        set.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        final float startScaleFinal = startScale;
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                if(oritentation != getResources().getConfiguration().orientation){
                    finish();
                    return;
                }
                // Animate the four positioning/sizing properties in parallel, back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(show, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(show, View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(show, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(show, View.SCALE_Y, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(lamp, "alpha", 1.0f, 0.0f));;
                set.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
