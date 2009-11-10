/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.widget.slidingtabs;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.remote.R;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * Container for a tabbed window view. This object holds two children: a set of
 * tab labels that the user clicks to select a specific tab, and a FrameLayout
 * object that displays the contents of that page. The individual elements are
 * typically controlled using this container object, rather than setting values
 * on the child elements themselves.
 */
public class SlidingTabHost extends FrameLayout implements ViewTreeObserver.OnTouchModeChangeListener {

	private SlidingTabWidget mTabWidget;
	private FrameLayout mTabContent;
	private List<SlidingTabSpec> mTabSpecs = new ArrayList<SlidingTabSpec>(2);
	/**
	 * This field should be made private, so it is hidden from the SDK. {@hide
	 * }
	 */
	protected int mCurrentTab = -1;
	private View mCurrentView = null;
	/**
	 * This field should be made private, so it is hidden from the SDK. {@hide
	 * }
	 */
	protected LocalActivityManager mLocalActivityManager = null;
	private OnTabChangeListener mOnTabChangeListener;

	public SlidingTabHost(Context context) {
		super(context);
		initTabHost();
	}

	public SlidingTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		initTabHost();
	}

	private final void initTabHost() {
		setFocusableInTouchMode(true);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

		mCurrentTab = -1;
		mCurrentView = null;
	}

	/**
	 * Get a new {@link SlidingTabSpec} associated with this tab host.
	 * 
	 * @param tag
	 *            required tag of tab.
	 */
	public SlidingTabSpec newTabSpec(String tag, String label, int resActiveIcon, int resInactiveIcon) {
		return new SlidingTabSpec(tag, label, resActiveIcon, resInactiveIcon);
	}

	/**
	 * <p>
	 * Call setup() before adding tabs if loading SlidingTabHost using
	 * findViewById(). <i><b>However</i></b>: You do not need to call setup()
	 * after getTabHost() in {@link android.app.TabActivity TabActivity}.
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * mTabHost = (SlidingTabHost) findViewById(R.id.tabhost);
	 * mTabHost.setup();
	 * mTabHost.addTab(TAB_TAG_1, "Hello, world!", "Tab 1");
	 */
	public void setup() {
		mTabWidget = (SlidingTabWidget) findViewById(R.id.slidingtabs);
		if (mTabWidget == null) {
			throw new RuntimeException("Your SlidingTabHost must have a SlidingTabWidget whose id attribute is 'R.id.slidingtabs'");
		}

		mTabWidget.setTabSelectionListener(new SlidingTabWidget.OnTabSelectionChanged() {
			public void onTabSelectionChanged(int tabIndex, boolean clicked) {
				setCurrentTab(tabIndex);
				if (clicked) {
					mTabContent.requestFocus(View.FOCUS_FORWARD);
				}
			}
		});

		mTabContent = (FrameLayout) findViewById(R.id.slidingtabcontent);
		if (mTabContent == null) {
			throw new RuntimeException("Your SlidingTabHost must have a FrameLayout whose id attribute is 'android.R.id.tabcontent'");
		}
		mTabWidget.setTabContent(mTabContent);
		
	}

	/**
	 * If you are using {@link SlidingTabSpec#setContent(android.content.Intent)}, this
	 * must be called since the activityGroup is needed to launch the local
	 * activity.
	 * 
	 * This is done for you if you extend {@link android.app.TabActivity}.
	 * 
	 * @param activityGroup
	 *            Used to launch activities for tab content.
	 */
	public void setup(LocalActivityManager activityGroup) {
		setup();
		mLocalActivityManager = activityGroup;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		final ViewTreeObserver treeObserver = getViewTreeObserver();
		if (treeObserver != null) {
			treeObserver.addOnTouchModeChangeListener(this);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		final ViewTreeObserver treeObserver = getViewTreeObserver();
		if (treeObserver != null) {
			treeObserver.removeOnTouchModeChangeListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void onTouchModeChanged(boolean isInTouchMode) {
		if (!isInTouchMode) {
			// leaving touch mode.. if nothing has focus, let's give it to
			// the indicator of the current tab
			if (!mCurrentView.hasFocus() || mCurrentView.isFocused()) {
				mTabWidget.getChildTabViewAt(mCurrentTab).requestFocus();
			}
		}
	}

	/**
	 * Add a tab.
	 * 
	 * @param tabSpec
	 *            Specifies how to create the indicator and content.
	 */
	public void addTab(SlidingTabSpec tabSpec) {
		
//		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View tabIndicator = inflater.inflate(R.layout.tab_indicator, mTabWidget, false);

		if (tabSpec.mContentStrategy == null) {
			throw new IllegalArgumentException("you must specify a way to create the tab content");
		}

		mTabWidget.addTab(tabSpec); //.setOnKeyListener(mTabKeyListener);
//		mTabWidget.updateOnKeyListener(mTabKeyListener);
		mTabSpecs.add(tabSpec);

		if (mCurrentTab == -1) {
			setCurrentTab(0);
		}
	}

	/**
	 * Removes all tabs from the tab widget associated with this tab host.
	 */
	public void clearAllTabs() {
		mTabWidget.removeAllViews();
		initTabHost();
		mTabContent.removeAllViews();
		mTabSpecs.clear();
		requestLayout();
		invalidate();
	}

	public SlidingTabWidget getTabWidget() {
		return mTabWidget;
	}

	public int getCurrentTab() {
		return mCurrentTab;
	}

	public String getCurrentTabTag() {
		if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
			return mTabSpecs.get(mCurrentTab).getTag();
		}
		return null;
	}

	public View getCurrentTabView() {
		if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
			return mTabWidget.getChildTabViewAt(mCurrentTab);
		}
		return null;
	}

	public View getCurrentView() {
		return mCurrentView;
	}

	public void setCurrentTabByTag(String tag) {
		int i;
		for (i = 0; i < mTabSpecs.size(); i++) {
			if (mTabSpecs.get(i).getTag().equals(tag)) {
				setCurrentTab(i);
				break;
			}
		}
	}

	/**
	 * Get the FrameLayout which holds tab content
	 */
	public FrameLayout getTabContentView() {
		return mTabContent;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					final int tabIndex = mCurrentTab - 1;
					if (tabIndex >= 0) {
						setCurrentTab(tabIndex);
						mTabWidget.moveTo(tabIndex);
					}
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					final int tabIndex = mCurrentTab + 1;
					if (tabIndex <= mTabWidget.getTabCount()) {
						setCurrentTab(tabIndex);
						mTabWidget.moveTo(tabIndex);
					}
				}
				return true;
			default:
				return super.dispatchKeyEvent(event);
		}
	}


	@Override
	public void dispatchWindowFocusChanged(boolean hasFocus) {
		View v = mCurrentView;
		if (v != null) {
			mCurrentView.dispatchWindowFocusChanged(hasFocus);
		}
	}

	public void setCurrentTab(int index) {
		if (index < 0 || index >= mTabSpecs.size()) {
			return;
		}

		if (index == mCurrentTab) {
			return;
		}

		// notify old tab content
		if (mCurrentTab != -1) {
			mTabSpecs.get(mCurrentTab).mContentStrategy.tabClosed();
		}

		mCurrentTab = index;
		final SlidingTabHost.SlidingTabSpec spec = mTabSpecs.get(index);

		// Call the tab widget's focusCurrentTab(), instead of just
		// selecting the tab.
//		mTabWidget.focusCurrentTab(mCurrentTab);

		// tab content
		mCurrentView = spec.mContentStrategy.getContentView();

		if (mCurrentView.getParent() == null) {
			mTabContent.addView(mCurrentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		}

		if (!mTabWidget.hasFocus()) {
			// if the tab widget didn't take focus (likely because we're in
			// touch mode)
			// give the current tab content view a shot
			mCurrentView.requestFocus();
		}

		// mTabContent.requestFocus(View.FOCUS_FORWARD);
		invokeOnTabChangeListener();
	}

	/**
	 * Register a callback to be invoked when the selected state of any of the
	 * items in this list changes
	 * 
	 * @param l
	 *            The callback that will run
	 */
	public void setOnTabChangedListener(OnTabChangeListener l) {
		mOnTabChangeListener = l;
	}

	private void invokeOnTabChangeListener() {
		if (mOnTabChangeListener != null) {
			mOnTabChangeListener.onTabChanged(getCurrentTabTag());
		}
	}

	/**
	 * Interface definition for a callback to be invoked when tab changed
	 */
	public interface OnTabChangeListener {
		void onTabChanged(String tabId);
	}

	/**
	 * Makes the content of a tab when it is selected. Use this if your tab
	 * content needs to be created on demand, i.e. you are not showing an
	 * existing view or starting an activity.
	 */
	public interface TabContentFactory {
		/**
		 * Callback to make the tab contents
		 * 
		 * @param tag
		 *            Which tab was selected.
		 * @return The view to display the contents of the selected tab.
		 */
		View createTabContent(String tag);
	}

	/**
	 * A sliding tab has an active icon, a passive icon, a big icon and a text.A tab has a tab indicator, content, and a tag that is used to keep track
	 * of it. This builder helps choose among these options.
	 * 
	 * For the tab indicator, your choices are: 1) set a label 2) set a label
	 * and an icon
	 * 
	 * For the tab content, your choices are: 1) the id of a {@link View} 2) a
	 * {@link TabContentFactory} that creates the {@link View} content. 3) an
	 * {@link Intent} that launches an {@link android.app.Activity}.
	 */
	public class SlidingTabSpec {

		private String mTag;
		private int mBigIcon = -1;
		
		private final int mActiveIcon;
		private final int mInactiveIcon;
		private final String mLabel;
		
		private ContentStrategy mContentStrategy;

		private SlidingTabSpec(String tag, String label, int resActiveIcon, int resInactiveIcon) {
			mTag = tag;
			mLabel = label;
			mActiveIcon = resActiveIcon;
			mInactiveIcon = resInactiveIcon;
		}
		
		/**
		 * Sets the big icon display in the content zone while sliding.
		 */
		public SlidingTabSpec setBigIcon(int resBigIcon) {
			mBigIcon = resBigIcon;
			return this;
		}

		/**
		 * Specify the id of the view that should be used as the content of the
		 * tab.
		 */
		public SlidingTabSpec setContent(int viewId) {
			mContentStrategy = new ViewIdContentStrategy(viewId);
			return this;
		}

		/**
		 * Specify a {@link android.widget.TabHost.TabContentFactory} to use to
		 * create the content of the tab.
		 */
		public SlidingTabSpec setContent(TabContentFactory contentFactory) {
			mContentStrategy = new FactoryContentStrategy(mTag, contentFactory);
			return this;
		}

		/**
		 * Specify an intent to use to launch an activity as the tab content.
		 */
		public SlidingTabSpec setContent(Intent intent) {
			mContentStrategy = new IntentContentStrategy(mTag, intent);
			return this;
		}

		public String getTag() {
			return mTag;
		}
		public int getActiveIconResource() {
			return mActiveIcon;
		}
		public int getInactiveIconResource() {
			return mInactiveIcon;
		}
		public String getLabel() {
			return mLabel;
		}
		public int getBigIconResource() {
			return mBigIcon;
		}
		
		
	}

	/**
	 * Specifies what you do to manage the tab content.
	 */
	private static interface ContentStrategy {

		/**
		 * Return the content view. The view should may be cached locally.
		 */
		View getContentView();

		/**
		 * Perhaps do something when the tab associated with this content has
		 * been closed (i.e make it invisible, or remove it).
		 */
		void tabClosed();
	}

	/**
	 * How to create the tab content via a view id.
	 */
	private class ViewIdContentStrategy implements ContentStrategy {

		private final View mView;

		private ViewIdContentStrategy(int viewId) {
			mView = mTabContent.findViewById(viewId);
			if (mView != null) {
				mView.setVisibility(View.GONE);
			} else {
				throw new RuntimeException("Could not create tab content because " + "could not find view with id " + viewId);
			}
		}

		public View getContentView() {
			mView.setVisibility(View.VISIBLE);
			return mView;
		}

		public void tabClosed() {
			mView.setVisibility(View.GONE);
		}
	}

	/**
	 * How tab content is managed using {@link TabContentFactory}.
	 */
	private class FactoryContentStrategy implements ContentStrategy {
		private View mTabContent;
		private final CharSequence mTag;
		private TabContentFactory mFactory;

		public FactoryContentStrategy(CharSequence tag, TabContentFactory factory) {
			mTag = tag;
			mFactory = factory;
		}

		public View getContentView() {
			if (mTabContent == null) {
				mTabContent = mFactory.createTabContent(mTag.toString());
			}
			mTabContent.setVisibility(View.VISIBLE);
			return mTabContent;
		}

		public void tabClosed() {
			mTabContent.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * How tab content is managed via an {@link Intent}: the content view is the
	 * decorview of the launched activity.
	 */
	private class IntentContentStrategy implements ContentStrategy {

		private final String mTag;
		private final Intent mIntent;

		private View mLaunchedView;

		private IntentContentStrategy(String tag, Intent intent) {
			mTag = tag;
			mIntent = intent;
		}

		public View getContentView() {
			if (mLocalActivityManager == null) {
				throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
			}
			final Window w = mLocalActivityManager.startActivity(mTag, mIntent);
			final View wd = w != null ? w.getDecorView() : null;
			if (mLaunchedView != wd && mLaunchedView != null) {
				if (mLaunchedView.getParent() != null) {
					mTabContent.removeView(mLaunchedView);
				}
			}
			mLaunchedView = wd;

			// XXX Set FOCUS_AFTER_DESCENDANTS on embedded activities for now so
			// they can get
			// focus if none of their children have it. They need focus to be
			// able to
			// display menu items.
			//
			// Replace this with something better when Bug 628886 is fixed...
			//
			if (mLaunchedView != null) {
				mLaunchedView.setVisibility(View.VISIBLE);
				mLaunchedView.setFocusableInTouchMode(true);
				((ViewGroup) mLaunchedView).setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
			}
			return mLaunchedView;
		}

		public void tabClosed() {
			if (mLaunchedView != null) {
				mLaunchedView.setVisibility(View.GONE);
			}
		}
	}

}
