package com.folioreader.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;

import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.fragments.HorizontalPageFragment;

/**
 * Author: Muhammad Shahab
 * Date: 5/8/17.
 * Description: Customize WebView class, It scroll the WebView horizontally
 *              page by page and restrict WebView to scroll vertically.
 */
public class HorizontalWebView extends WebView {

    private final String TAG = "HorizontalWebView";
    private ActionMode.Callback mActionModeCallback;
    private HorizontalPageFragment mFolioPageFragment;
    private HorizontalPageFragment.FolioPageFragmentCallback mActivityCallback;
    private float start_x = -1;
    private float start_y = -1;
    private int current_y = 0;
    private int pageLeftCount = 3;
    private int pageRightCount = 3;
    private final int PAGE_PADDING = 20;

    Animation slideLeftAnimation;
    Animation slideRightAnimation;

    private final int SLIDING_THRESHOLD = 100;



    public static interface ScrollListener {
        void onScrollChange(int percent);
    }

    private ScrollListener mScrollListener;

    /**
     * First Constructor
     * @param context
     */
    public HorizontalWebView(Context context) {
        super(context);
    }

    /**
     * Second Constructor
     * @param context
     * @param attrs
     */
    public HorizontalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Third Constructor
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public HorizontalWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Fourth Constructor
     * @param context
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalWebView(Context context, AttributeSet attrs,
                             int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        /*Initializing sliding animation of Left and Right*/
        slideLeftAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_left);
        slideRightAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_right);
    }

    /**
     * @purpose It keeps the object of ScrollListener so
     *          It can trigger the event Whenever scroll change
     *
     * @param scrollListener
     */
    public void setScrollListener(ScrollListener scrollListener) {
        mScrollListener = scrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mActivityCallback = (FolioActivity) getContext();
        mActivityCallback.hideToolBarIfVisible();



        if (mScrollListener != null)
            mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    /**
     * @usage It gives the WebView content height
     * @return Integer
     */
    public int getContentHeightVal() {
        int height = (int) Math.floor(this.getContentHeight() * this.getScale());
        return height;
    }

    /**
     * @usage it represent the WebView measured height
     * @return Integer
     */
    public int getWebviewHeight() {
        return this.getMeasuredHeight();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return this.dummyActionMode();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return this.dummyActionMode();
    }

    public ActionMode dummyActionMode() {
        return new ActionMode() {
            @Override
            public void setTitle(CharSequence title) {
            }

            @Override
            public void setTitle(int resId) {
            }

            @Override
            public void setSubtitle(CharSequence subtitle) {
            }

            @Override
            public void setSubtitle(int resId) {
            }

            @Override
            public void setCustomView(View view) {
            }

            @Override
            public void invalidate() {
            }

            @Override
            public void finish() {
            }

            @Override
            public Menu getMenu() {
                return null;
            }

            @Override
            public CharSequence getTitle() {
                return null;
            }

            @Override
            public CharSequence getSubtitle() {
                return null;
            }

            @Override
            public View getCustomView() {
                return null;
            }

            @Override
            public MenuInflater getMenuInflater() {
                return null;
            }
        };
    }

    /**
     * @purpose It save the fragment in which WebView resides
     * @param folioPageFragment
     */
    public void setFragment(HorizontalPageFragment folioPageFragment) {
        mFolioPageFragment = folioPageFragment;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mActivityCallback = (FolioActivity)getContext();
        int action = event.getAction();

        switch (action)
        {
            /*When user touch the screen to slide*/
            case (MotionEvent.ACTION_DOWN):

                /**
                 * getting X and Y position
                 */
                start_x = event.getX();
                start_y = event.getY();
                break;



            /*When user move the slide*/
            case (MotionEvent.ACTION_MOVE):

                /* Below logic is to block the parent touch where ever
                  this view will be placed the below logic will disable
                  the parent touch */
                if (getParent() != null)
                {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                /*This will block the vertical scroll of WebView*/
                if (start_y-event.getY()>20 || start_y-event.getY()<-20)
                    return true;


                break;

            /*When user move his thumb up to stop the slide*/
            case (MotionEvent.ACTION_UP):



                 /*right to left*/
                if(start_x-event.getX()>SLIDING_THRESHOLD)
                {

                    Log.d(TAG, "onTouchEvent: RTL ScrollY " + getScrollY());
                    Log.d(TAG, "onTouchEvent: RTL Height" + getContentHeightVal());

                    /**/
                    if (getCurrentPage()+1<getTotalPages())
                    {
                        turnPageRight();
                    }
                    else
                    {
                        ((FolioActivity)mFolioPageFragment.getActivity()).loadNextPage();
                    }

                    return true;
                }

                /*Left to right*/
                if(event.getX()-start_x>SLIDING_THRESHOLD)
                {
                    Log.d(TAG, "onTouchEvent: LTR ScrollY " + getScrollY());
                    Log.d(TAG, "onTouchEvent: LTR Height" + getContentHeightVal());

                    /*If the current page is greater then turn the page left*/
                    if (getCurrentPage()>0) {

                        turnPageLeft();
                    }


                    /*This condition is used when the current page is
                      zero but the scroll Y is not at the zero position
                       in the case of first page loading completely i
                        needed to put this check here */
                    else if (getCurrentPage()==0&&getScrollY()>0)
                    {
                        scrollTo(0,0);
                    }


                    /*Load previous chapter of the book because chapter
                      is at the start and user want to slide left*/
                    else
                    {
                        ((FolioActivity)mFolioPageFragment.getActivity()).loadPrevPage();
                    }

                    return true;
                }
                mActivityCallback.hideOrshowToolBar();
                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * @purpose It turns the page right to left with left sliding animation
     */
    private void turnPageLeft() {
            int scrollY = getPrevPagePosition();
            current_y = scrollY;
            startAnimation(slideLeftAnimation);
            scrollTo(0, scrollY + (pageLeftCount *PAGE_PADDING));

            pageLeftCount++;

            if (pageRightCount >3)
                pageRightCount--;
    }


    /**
     * @purpose It turns the page right to left with right sliding animation
     */
    private void turnPageRight() {
            int scrollY = getNextPagePosition();
            current_y = scrollY;
            startAnimation(slideRightAnimation);
            scrollTo(0, scrollY - (pageRightCount * PAGE_PADDING));


            pageRightCount++;

            if (pageLeftCount >3)
                pageLeftCount--;
    }

    /**
     * @purpose It provides the next page position so WebView can scroll to next page
     * @return Integer
     */
    private int getNextPagePosition() {
        int nextPage = getCurrentPage() + 1;
        Log.d(TAG, "getNextPagePosition: "+nextPage);
        return (int) Math.ceil(nextPage * getWebviewHeight());
    }


    /**
     * @purpose It provides the previous page position so WebView can scroll to previous page
     * @return
     */
    private int getPrevPagePosition() {
        int prevPage = getCurrentPage() - 1;
        Log.d(TAG, "getPrevPagePosition: "+prevPage);
        return (int) Math.ceil(prevPage * getWebviewHeight());
    }

    /**
     * @purpose It calculates the current page by dividing the current Y scroll to WebView height
     * @return Integer
     */
    public int getCurrentPage()
    {
        int currentPage = (int) (Math.ceil((double) current_y / getWebviewHeight()));
        Log.d(TAG, "getCurrentPage: " + currentPage);
        return currentPage;
    }

    /**
     * @purpose It calculates total pages by dividing the WebView content height to WebView height
     * @return Integer
     */
    public int getTotalPages()
    {
        int totalPages =
                (int) Math.ceil((double) getContentHeightVal()
                        / getWebviewHeight());

        Log.d(TAG, "getTotalPages: " + totalPages);
        return totalPages;

    }
}
