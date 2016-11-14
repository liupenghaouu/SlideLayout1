package com.uu.slidelayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import static android.content.ContentValues.TAG;

/**
 * Created by penghao on 2016/11/14.
 * 可自己滑动的listView 的 item 的自定义 控件
 */

public class SwipeLayout extends FrameLayout {

    private View contentView;
    private View deleteView;
    private ViewDragHelper helper;
    private int contentHeight;
    private int contentWidth;
    private int deleteHeight;
    private int deleteWidth;
    private int dragRange;          //控件的拖拽范围
    private float fraction;
    private SwipeState mCurrentState = SwipeState.close;
    //状态

    private SwipeLayoutManager swipeLayoutManager = SwipeLayoutManager.getInstance();

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SwipeLayout(Context context) {
        super(context);
        init();
    }


    enum SwipeState {
        close, open;
    }

    /**
     * 初始化控件，配置相应的参数
     */
    private void init() {

        helper = ViewDragHelper.create(this, callback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = getChildAt(0);
        deleteView = getChildAt(1);

    }


    /**
     * 事件的分发交由VIewDragHelper  去处理
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean result = helper.shouldInterceptTouchEvent(ev);
        //是否可以滑动 -----------,   不可以，就关闭
        //如果当前有打开的，则需要直接拦截，交给onTouch处理
        if (!swipeLayoutManager.isShouldSiwpe(this)) {
            //先关闭已经打开的layout
            swipeLayoutManager.closeCurrentLayout();
            result = true;
        }


        return result;
    }

    private float downX = 0;
    private float downY = 0;

    /**
     * 触摸事件交给  helper 处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果当前有打开的，则下面的逻辑不能执行
        if (!swipeLayoutManager.isShouldSiwpe(this)) {
            requestDisallowInterceptTouchEvent(true);
            return true;
        }

        //这有当当前滑动 的控件的item 已经被打开，且 当前滑动的是 已经被打开的控件，才走此switchcase  流程
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();

                float delatX = moveX - downX;
                float delatY = moveY - downY;
                //  比较绝对值的大小
                if (Math.abs(delatX) > Math.abs(delatY)) {
                    //表示移动是偏向于水平方向，那么应该SwipeLayout应该处理，请求listview不要拦截
                    requestDisallowInterceptTouchEvent(true);
                }
                // 更新初始坐标
                downX = moveX;
                downY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        helper.processTouchEvent(event);
        return true;
    }

    /**
     * 此方法运行在measure 之后，可在此方法中得到控件的宽高
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        contentHeight = contentView.getMeasuredHeight();
        contentWidth = contentView.getMeasuredWidth();
        deleteHeight = deleteView.getMeasuredHeight();
        deleteWidth = deleteView.getMeasuredWidth();
        dragRange = deleteWidth;
    }

    /**
     * 重新摆放控件的位置
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int newLeft = getPaddingLeft();
        int newTop = getPaddingTop();
        contentView.layout(newLeft, newTop, newLeft + contentWidth, newTop + contentHeight);
        deleteView.layout(contentView.getRight(), newTop, contentView.getRight() + deleteWidth,
                contentView.getTop() + deleteHeight);
    }

    /**
     * ViewDraghelper 的回调接口的实现内部类
     */
    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {


        @Override
        public boolean tryCaptureView(View view, int i) {
            return view == contentView || view == deleteView;
        }

        /**
         *      横向的拖拽范围
         */
        @Override
        public int getViewHorizontalDragRange(View child) {

            return dragRange;
        }

        /**
         *  控制控件横向移动
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            if (child == contentView) {      //内容的范围控制
                if (left > 0) {
                    left = 0;
                }
                if (left < -deleteWidth) {
                    left = -deleteWidth;
                }
            } else if (child == deleteView) {
                if (left < contentWidth - deleteWidth) {
                    left = contentWidth - deleteWidth;
                }
                if (left > contentWidth) {
                    left = contentWidth;
                }
            }
            return left;
        }

        /**
         *  伴随动画的实现
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == contentView) {
                contentView.layout(left, top, left + contentWidth, top + contentHeight);
                deleteView.layout(deleteView.getLeft() + dx, deleteView.getTop() + dy,
                        deleteView.getRight() + dx, deleteView.getBottom() + dy);
            } else if (changedView == deleteView) {
                deleteView.layout(left, top, left + deleteWidth, top + deleteHeight);
                contentView.layout(contentView.getLeft() + dx, contentView.getTop() + dy,
                        contentView.getRight() + dx, contentView.getBottom() + dy);
            }
            //计算拖拽的百分比

            fraction = -contentView.getLeft() * 1f / dragRange;
            if (listener != null) {
                listener.swiping(fraction);
            }

            //  当Item 条目打开或者关闭的时候 ，移除或者添加    SwipeLayout 到 manager 中
            if (mCurrentState != SwipeState.close && fraction == 0) {

                swipeLayoutManager.clearCurrentLayout();
                mCurrentState = SwipeState.close;
                if (listener != null) {
                    listener.close();
                }

            } else if (mCurrentState != SwipeState.open && fraction == 1) {

                swipeLayoutManager.setCurrentLayout(SwipeLayout.this);
                mCurrentState = SwipeState.open;
                if (listener != null) {
                    listener.open();
                }
            }
            Log.e(TAG, "contentView.getLeft()==" + contentView.getLeft());
            Log.e(TAG, "dragRange ==  " + dragRange);
            Log.e(TAG, "fraction ==  " + fraction);

        }

        /**
         *  手指释放时的动画
         *  通过contentView的 左边  位置，来判断是否该 关闭或者  打开
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (fraction > 0.5) {  //开
                open();
            } else {
                close();
            }

            Log.e(TAG, "速度 == " + xvel);
            //根据速度  判断应该开还是关
            if (xvel > 200 && mCurrentState != SwipeState.open) {
                open();
            } else if (xvel < -200 && mCurrentState != SwipeState.close) {
                close();
            }
        }
    };

    /**
     * 关闭侧滑菜单选项
     */
    public void close() {
        helper.smoothSlideViewTo(contentView, getPaddingLeft(), contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(this);
    }


    /**
     * 打开侧滑菜单选项
     */
    public void open() {
        helper.smoothSlideViewTo(contentView, -deleteWidth, contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 如果正在执行打开或者关闭就   刷新一下
     */
    @Override
    public void computeScroll() {
        if (helper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private SwipeListener listener;

    public void setOnSwipeListener(SwipeListener listener) {
        this.listener = listener;
    }

    public interface SwipeListener {
        void open();

        void close();

        void swiping(float fraction);
    }
}
