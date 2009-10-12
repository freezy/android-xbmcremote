/*
 * Code taken from jasta's five project:
 *   http://code.google.com/p/five/
 * 
 * Much of this logic was taken from Romain Guy's Shelves project:
 * 
 *   http://code.google.com/p/shelves/
 */
package org.xbmc.android.widget;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Abstracts a mechanism for determining an appropriate time to load images into
 * an animating ListView. That is, only load the images when the list view is
 * not flinging.
 * 
 * To use, you must make sure that you call through to
 * {@link onScrollStateChanged} and {@link onTouch}.
 * 
 * This class was inspired by Romain Guy's ShelvesActivity. Rather, it was
 * blatantly lifted from it :)
 */
public class IdleListDetector {
	
	private final ScrollHandler mScrollHandler;
	private final OnListIdleListener mListener;

	/*
	 * Time to wait before loading the images while the user still has their
	 * finger grabbing the list but is not currently flinging.
	 */
	private static final int DELAY_IDLE_DETECTION = 550;
	private static final int DELAY_FAST_IDLE_DETECTION = DELAY_IDLE_DETECTION;

	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	/* Special considerations for FastScrollView. */
	private int mFastScrollState = FastScrollView.SCROLL_STATE_FAST_IDLE;

	private boolean mFingerUp = true;
	private boolean mPending = false;

	public IdleListDetector(OnListIdleListener l) {
		mListener = l;
		mScrollHandler = new ScrollHandler();
	}

	public boolean isListIdle() {
		if (mScrollState == OnScrollListener.SCROLL_STATE_FLING)
			return false;

		if (mPending == true)
			return false;

		return true;
	}

	public interface OnListIdleListener {
		void onListIdle();
	}

	public boolean onTouch(View v, MotionEvent ev) {
		int action = ev.getAction();

		mFingerUp = (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL);

		if (mFingerUp && mScrollState != OnScrollListener.SCROLL_STATE_FLING)
			mScrollHandler.sendListIdle(true);

		return false;
	}

	public void onFastScrollStateChanged(AbsListView view, int scrollState) {
		if (mFastScrollState == FastScrollView.SCROLL_STATE_FAST_SCROLLING && scrollState != FastScrollView.SCROLL_STATE_FAST_SCROLLING)
			mScrollHandler.sendListIdle(true);
		else if (scrollState == FastScrollView.SCROLL_STATE_FAST_SCROLLING)
			mScrollHandler.sendListIdle(DELAY_FAST_IDLE_DETECTION);

		if (scrollState == FastScrollView.SCROLL_STATE_FAST_IDLE)
			mFingerUp = true;

		mFastScrollState = scrollState;
		mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollState == OnScrollListener.SCROLL_STATE_FLING && scrollState != OnScrollListener.SCROLL_STATE_FLING)
			mScrollHandler.sendListIdle(mFingerUp);
		else if (scrollState == OnScrollListener.SCROLL_STATE_FLING)
			mScrollHandler.cancelListIdle();

		mScrollState = scrollState;
	}

	private class ScrollHandler extends Handler {
		private static final int MSG_LIST_IDLE = 0;

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LIST_IDLE:
				mPending = false;
				mListener.onListIdle();
				break;
			}
		}

		public void sendListIdle(int delay) {
			Message m = obtainMessage(MSG_LIST_IDLE);
			removeMessages(MSG_LIST_IDLE);
			mPending = true;
			sendMessageDelayed(m, delay);
		}

		public void sendListIdle(boolean fingerUp) {
			sendListIdle(fingerUp ? 0 : DELAY_IDLE_DETECTION);
		}

		public void cancelListIdle() {
			mPending = false;
			removeMessages(MSG_LIST_IDLE);
		}
	}
}
