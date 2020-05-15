package com.rikkatheworld.view_pager_demo.pager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

public class TwoViewPager extends ViewPager {
    private static final String TAG = "TwoViewPager";

    private boolean scrolling;
    private float downX;
    private float downY;
    private float downScrollX;
    private float downScrollY;

    VelocityTracker velocityTracker;
    ViewConfiguration viewConfiguration;
    OverScroller overScroller;
    private float maxVelocity;
    private double minVelocity;

    public TwoViewPager(@NonNull Context context) {
        this(context, null);
    }

    public TwoViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        velocityTracker = VelocityTracker.obtain();
        viewConfiguration = ViewConfiguration.get(context);
        overScroller = new OverScroller(context);
        maxVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        minVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        int childTop = 0;
        int childRight = getWidth();
        int childBottom = getHeight();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(childLeft, childTop, childRight, childBottom);
            childLeft += getWidth();
            childRight += getWidth();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear();
        }
        velocityTracker.addMovement(ev);

        boolean result = false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                scrolling = false;
                downX = ev.getX();
                downY = ev.getY();
                downScrollX = getScrollX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = downX - ev.getX() + downScrollX;
                if (!scrolling) {
                    if (Math.abs(dx) > viewConfiguration.getScaledPagingTouchSlop()) {
                        scrolling = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        result = true;
                    }
                }
                break;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear();
        }
        velocityTracker.addMovement(ev); //Tracker计算一个新的事件，这个事件产生的速度

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                scrolling = false;
                downX = ev.getX();
                downY = ev.getY();
                downScrollX = getScrollX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = downX - ev.getX() + downScrollX;
                if (dx > getWidth()) {
                    dx = getWidth();
                } else if (dx < 0) {
                    dx = 0;
                }
                scrollTo((int) dx, 0);
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                float vx = velocityTracker.getXVelocity();
                int scrollX = getScrollX();
                int targetPage;
                if (Math.abs(vx) < minVelocity) {
                    targetPage = scrollX > getWidth() / 2 ? 1 : 0;  //滑动的偏移大于宽度一般，则去到下一个页面
                } else {
                    targetPage = vx < 0 ? 1 : 0;
                }
                int scrollDistance = targetPage == 1 ? (getWidth() - scrollX) : -scrollX;
                overScroller.startScroll(getScrollX(), 0, scrollDistance, 0);
                postInvalidateOnAnimation(); //在draw方法里onDraw前会自动调用computeScroll
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
            postInvalidateOnAnimation();
        }
    }
}
