package com.uu.slidelayout;

/**
 * Created by penghao on 2016/11/14.
 * <p>
 * 滑动条的管理类，保证当前这能有一个条目滑动
 */

public class SwipeLayoutManager {

    private static SwipeLayoutManager swipeLayoutManger = new SwipeLayoutManager();

    private SwipeLayout currentLayout = null;


    private SwipeLayoutManager() {
    }

    public static SwipeLayoutManager getInstance() {
        return swipeLayoutManger;
    }

    /**
     * 得到当前的Swipelayout对象
     */
    public SwipeLayout getCurrentLayout() {
        return currentLayout;
    }

    /**
     * 设置SwipeLayout对象
     *
     * @param currentLayout
     */
    public void setCurrentLayout(SwipeLayout currentLayout) {
        this.currentLayout = currentLayout;
    }


    /**
     * 判断当前是否能够滑动，如果没有打开的，则可以滑动
     * 如果有打开的，则判断打开的Layout 和当前按下的layout 是否是同一个
     */
    public boolean isShouldSiwpe(SwipeLayout swipeLayout) {
        if (currentLayout == null) {
            //当前没有打开的 layout,可以滑动
            return true;
        } else {
            //判断是否与当前 按下的一致
            return currentLayout == swipeLayout;
        }
    }

    /**
     * 清空当前所记录的已经打开的layout
     */
    public void clearCurrentLayout() {
        currentLayout = null;
    }

    /**
     * 关闭当前的SwipeLayout
     */
    public void closeCurrentLayout() {
        if (currentLayout != null) {
            currentLayout.close();
        }
    }
}
