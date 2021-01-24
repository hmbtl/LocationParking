package com.hmbtl.locationparking.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anar on 11/27/17.
 */

public class ProgressButton  extends android.support.v7.widget.AppCompatButton {

    private enum State {
        PROGRESS, IDLE
    }

    public interface OnAnimationEndListener{
        void onAnimationEnd();
    }

    private State mState = State.IDLE;
    private boolean mIsMorphingInProgress;
    private GradientDrawable mGradientDrawable;
    private AnimatorSet mMorphingAnimatorSet;
    private CircularAnimatedDrawable mAnimatedDrawable;
    private Context context;
    private long startTime;
    private Handler handler;

    private int mHeight, mWidth;
    private CharSequence mText, tempText;


    public ProgressButton(Context context) {
        super(context);
        init(context);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        if(getBackground() == null){
            mGradientDrawable = (GradientDrawable) getBackground();
        } else {
            mGradientDrawable = new GradientDrawable();
            mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
            mGradientDrawable.setColor(Color.BLACK);
        }
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        if(mState == State.PROGRESS){
            mText = text;
            text = null;
        } else {
            mText = text;
        }
        super.setText(text, type);

    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if (mState == State.PROGRESS && !mIsMorphingInProgress) {
            drawIndeterminateProgress(canvas);
        }
    }

    private void drawIndeterminateProgress(Canvas canvas) {
        if (mAnimatedDrawable == null) {

            int arcWidth = 15;

            mAnimatedDrawable = new CircularAnimatedDrawable(Color.WHITE, arcWidth);

            int offset = (getWidth() - getHeight()) / 2;

            int left = offset;
            int right = getWidth() - offset;
            int bottom = getHeight();
            int top = 0;

            mAnimatedDrawable.setBounds(left, top, right, bottom);
            mAnimatedDrawable.setCallback(this);
            mAnimatedDrawable.start();
        } else {
            mAnimatedDrawable.draw(canvas);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mAnimatedDrawable || super.verifyDrawable(who);
    }



    private void stopMorphAnimation(final OnAnimationEndListener onAnimationEndListener){


        if(mState != State.PROGRESS){
            return;
        }

        int initialWidth = getWidth();
        int initialHeight = getHeight();

        int initialCornerRadius = 1000;
        int finalCornerRadius = 0;

        mState = State.IDLE;
        mIsMorphingInProgress = true;


        setTextToNull();


        int toWidth = mWidth; //some random value...
        int toHeight = mHeight; //make it a perfect circle


        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(mGradientDrawable,
                        "cornerRadius",
                        initialCornerRadius,
                        finalCornerRadius);

        ValueAnimator widthAnimation = ValueAnimator.ofInt(initialWidth, toWidth);
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.width = val;
                setLayoutParams(layoutParams);
            }
        });

        ValueAnimator heightAnimation = ValueAnimator.ofInt(initialHeight, toHeight);
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = val;
                setLayoutParams(layoutParams);
            }
        });



        List<Animator> animators = new ArrayList<>();
        animators.add(cornerAnimation);
        animators.add(widthAnimation);
        animators.add(heightAnimation);

        if(getLayoutParams() instanceof ViewGroup.MarginLayoutParams){
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.getLayoutParams();

            int leftMargin = lp.leftMargin;
            int rightMargin = lp.rightMargin;

            int toMargin = (toWidth - initialWidth) / 2;

            ValueAnimator leftMarginAnimator = ValueAnimator.ofInt(leftMargin, leftMargin - toMargin);
            leftMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                    layoutParams.leftMargin = val;
                    setLayoutParams(layoutParams);
                }
            });

            ValueAnimator rightMarginAnimator = ValueAnimator.ofInt(rightMargin, rightMargin - toMargin);
            rightMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                    layoutParams.rightMargin = val;
                    setLayoutParams(layoutParams);
                }
            });

            animators.add(leftMarginAnimator);
            animators.add(rightMarginAnimator);

        }


        mMorphingAnimatorSet = new AnimatorSet();
        mMorphingAnimatorSet.setDuration(300);
        mMorphingAnimatorSet.playTogether(animators);
        mMorphingAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsMorphingInProgress = false;
                setText(mText);
                setClickable(true);
                if(onAnimationEndListener != null)
                    onAnimationEndListener.onAnimationEnd();
            }
        });
        mMorphingAnimatorSet.start();
    }


    public void stopAnimation(final OnAnimationEndListener onAnimationEndListener){

        long currentTime = System.currentTimeMillis();

        if(currentTime - startTime < 1000){

            long remainingTime = 1000 - (currentTime - startTime);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopMorphAnimation(onAnimationEndListener);
                }
            }, remainingTime);
        } else {
            stopMorphAnimation(onAnimationEndListener);
        }

    }

    public void stopAnimation(){

        long currentTime = System.currentTimeMillis();

        if(currentTime - startTime < 1000){

            long remainingTime = 1000 - (currentTime - startTime);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopMorphAnimation(null);
                }
            }, remainingTime);
        } else {
            stopMorphAnimation(null);
        }

    }

    public void stopWithoutDelay(){
        stopMorphAnimation(null);
    }




    public void startAnimation(){
        if(mState != State.IDLE){
            return;
        }

        startTime = System.currentTimeMillis();

        mWidth = getWidth();
        mHeight = getHeight();
        mText = getText();


        int initialWidth = getWidth();
        int initialHeight = getHeight();

        int initialCornerRadius = 0;
        int finalCornerRadius = 1000;


        mState = State.PROGRESS;
        mIsMorphingInProgress = true;

        setTextToNull();
        setClickable(false);


        int toWidth = getHeight(); //some random value...
        int toHeight = toWidth; //make it a perfect circle


        ObjectAnimator cornerAnimation =
                ObjectAnimator.ofFloat(mGradientDrawable,
                        "cornerRadius",
                        initialCornerRadius,
                        finalCornerRadius);

        ValueAnimator widthAnimation = ValueAnimator.ofInt(initialWidth, toWidth);
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.width = val;
                setLayoutParams(layoutParams);
            }
        });

        ValueAnimator heightAnimation = ValueAnimator.ofInt(initialHeight, toHeight);
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = val;
                setLayoutParams(layoutParams);
            }
        });




        List<Animator> animators = new ArrayList<>();
        animators.add(cornerAnimation);
        animators.add(widthAnimation);
        animators.add(heightAnimation);

        if(getLayoutParams() instanceof ViewGroup.MarginLayoutParams){
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.getLayoutParams();

            int leftMargin = lp.leftMargin;
            int rightMargin = lp.rightMargin;

            int toMargin = (initialWidth - toWidth) / 2;

            ValueAnimator leftMarginAnimator = ValueAnimator.ofInt(leftMargin, toMargin + leftMargin);
            leftMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                    layoutParams.leftMargin = val;
                    setLayoutParams(layoutParams);
                }
            });

            ValueAnimator rightMarginAnimator = ValueAnimator.ofInt(rightMargin, toMargin + rightMargin);
            rightMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                    layoutParams.rightMargin = val;
                    setLayoutParams(layoutParams);
                }
            });

            animators.add(leftMarginAnimator);
            animators.add(rightMarginAnimator);

        }




        mMorphingAnimatorSet = new AnimatorSet();
        mMorphingAnimatorSet.setDuration(300);
        mMorphingAnimatorSet.playTogether(animators);
        mMorphingAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsMorphingInProgress = false;
            }
        });
        mMorphingAnimatorSet.start();
    }



    private void setTextToNull(){
        tempText = mText;
        setText(null);
        mText = tempText;
    }


}
