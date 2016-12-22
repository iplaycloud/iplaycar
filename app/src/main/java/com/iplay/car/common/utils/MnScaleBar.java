package com.iplay.car.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by Administrator on 2016/7/28.
 */
public class MnScaleBar extends View {
    private Context mContext;
    private Rect mRect;
    private int max = 70;
    private int mCountScale;

    private int screenWidth = 720;

    private int mScaleMargin = 10;
    private int mScaleHeight = 20;

    private int mScaleHeight1=10;

    private int mScaleMaxHeight = mScaleHeight * 2;

    private int mScaleMaxHeight1=mScaleHeight1*2;

    private int mRectWidth = max * mScaleMargin;
    private int mRectHeight = 100;

    private int mRectHeight1=90;

    private Scroller mScroller;
    private int mScrollLastX;

    //private int mTempScale = screenWidth / mScaleMargin / 2;
    //private int mScreenMidCountScale = screenWidth / mScaleMargin / 2;

    private OnScrollListener onScrollListener;

    private String tag = MnScaleBar.class.getSimpleName();

    public MnScaleBar(Context context) {
        this(context, null);
    }

    public MnScaleBar(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MnScaleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        screenWidth = getPhoneW(mContext);
        mScroller = new Scroller(mContext);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                mRectWidth, mRectHeight);
        this.setLayoutParams(lp);
        mRect = new Rect(0, 0, mRectWidth, mRectHeight);
        Paint mPaint = new Paint();
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(mRect, mPaint);

        onDrawScale(canvas);
        //onDrawPointer(canvas);

        super.onDraw(canvas);
    }

    private void onDrawScale(Canvas canvas) {
        if (canvas == null)
            return;
        Paint mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
//        mPaint.setTextAlign(Paint.Align.CENTER);
//        mPaint.setTextSize(20);
        for (int i = 0; i < max; i++) {
            if (i != 0 && i != max) {
                if (i % 10 == 0) {
                    canvas.drawLine(i*mScaleMargin, mRectHeight, i*mScaleMargin, mRectHeight-mScaleMaxHeight, mPaint);
                    // 整值文字
//					canvas.drawText(String.valueOf(i), i * mScaleMargin,
//							mRectHeight - mScaleMaxHeight - 10, mPaint);

                } else if(i%5==0){
                    canvas.drawLine(i*mScaleMargin, mRectHeight, i*mScaleMargin, mRectHeight1-mScaleMaxHeight1, mPaint);
                }
                else {
                    canvas.drawLine(i*mScaleMargin, mRectHeight, i*mScaleMargin, mRectHeight-mScaleHeight, mPaint);
                }
            }
        }
    }

    private void onDrawPointer(Canvas canvas) {
        if (canvas == null)
            return;
        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(20);
        int countScale = screenWidth / mScaleMargin / 2;
        int finalX = mScroller.getFinalX();
        int tmpCountScale = (int) Math.rint((double) finalX
                / (double) mScaleMargin);
        mCountScale = tmpCountScale + countScale;
        if (onScrollListener != null) { // 回调方法
            onScrollListener.onScrollScale(mCountScale);
        }
        canvas.drawLine(countScale * mScaleMargin + finalX, mRectHeight,
                countScale * mScaleMargin + finalX, mRectHeight
                        - mScaleMaxHeight, mPaint);
        canvas.drawText(String.valueOf(mCountScale), countScale * mScaleMargin
                + finalX, mRectHeight - mScaleMaxHeight - 10, mPaint);

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }
        super.computeScroll();
    }
    

    public void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,
                dy);
    }

    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public interface OnScrollListener {
        void onScrollScale(int scale);
    }

    public static int getPhoneW(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        int disw = dm.widthPixels;
        return disw;
    }
}
