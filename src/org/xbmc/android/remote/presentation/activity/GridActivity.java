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

package org.xbmc.android.remote.presentation.activity;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.ListController;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.GridView;

/**
 * TODO do that properly
 * @author Team XBMC
 */
public class GridActivity extends AbsListActivity {
	
	public GridActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.blankgrid);
		
		// remove nasty top fading edge
		FrameLayout topFrame = (FrameLayout)findViewById(android.R.id.content);
		topFrame.setForeground(null);
		
		mListController = (ListController)getLastNonConfigurationInstance();
		if (mListController == null) {
			mListController = (ListController) getIntent().getSerializableExtra(ListController.EXTRA_LIST_CONTROLLER);
	
			mListController.findTitleView(findViewById(R.id.blanklist_outer_layout));
			mListController.findMessageView(findViewById(R.id.blanklist_outer_layout));
			mListController.onCreate(this, (GridView)findViewById(R.id.blanklist_list));
		}
		mConfigurationManager = ConfigurationManager.getInstance(this);
		mConfigurationManager.initKeyguard();
	}
}