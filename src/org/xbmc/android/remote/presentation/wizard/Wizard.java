package org.xbmc.android.remote.presentation.wizard;

import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.wizard.listener.PageCanFinishListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public abstract class Wizard<T> extends Activity {

	private ArrayList<WizardPage<T>> pages = new ArrayList<WizardPage<T>>();
	private ArrayList<WizardPage<T>> shownPagesStack = new ArrayList<WizardPage<T>>();
	private WizardPage<T> currentPage = null;
	private int currentPos = -1;

	private Button next;
	private Button prev;
	private FrameLayout main;
	private LinearLayout overlay;
	private TextView msg;
	
	public Wizard() {
		super();
	}
	
	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setup_wizard);
		main = (FrameLayout) findViewById(R.id.setup_wizard_root);
		next = (Button) findViewById(R.id.setup_button_next);
		prev = (Button) findViewById(R.id.setup_button_prev);
		
		next.setEnabled(false);
		prev.setEnabled(false);
		
		overlay = (LinearLayout) findViewById(R.id.setup_wizard_msg_overlay);
		msg = (TextView) findViewById(R.id.setup_wizard_msg);
		overlay.setVisibility(View.GONE);
		
		doSetupPages();
		checkButtons();
		for(WizardPage<T> page : pages) {
			page.init();
			main.addView(page, 0);
			page.setVisibility(View.GONE);
		}
		currentPage.setVisibility(View.VISIBLE);
		shownPagesStack.add(currentPage);
		next.setOnClickListener(currentPage.getNextClickListener());
	}
	
	public abstract void doSetupPages();
	
	void showNextPage() {
		
		if(currentPage != null && currentPage.getNextPage() != null) {
				currentPage.hide();
				currentPage = currentPage.getNextPage();
				currentPage.show();
				shownPagesStack.add(currentPage);
				next.setOnClickListener(currentPage.getNextClickListener());
		}else{
			if(currentPos < pages.size()) {
				WizardPage<T> page = pages.get(++currentPos);
				page.setInput(currentPage.getInput());
				checkButtons();
				currentPage.hide();
				currentPage = page;
				currentPage.show();
				shownPagesStack.add(currentPage);
				next.setOnClickListener(currentPage.getNextClickListener());
			}else{
				//finish wizard
			}
		}
	}
	
	void showPrevPage() {
		
	}
	
	void showBusyMessage(String msg) {
		this.msg.setText(msg);
		overlay.setVisibility(View.VISIBLE);
	}
	
	void removeBusyMessage() {
		overlay.setVisibility(View.GONE);
	}
	
	protected void addPage(final WizardPage<T> page) {
		if(currentPage == null) {
			currentPage = page;
			currentPos = 0;
		}
		pages.add(page);
		page.addCanFinishListener(new PageCanFinishListener() {
			public void canFinish(boolean b) {
				next.setEnabled(b);
			}
		});
		checkButtons();
	}
	
	private void checkButtons() {
		if(currentPos == pages.size() -1 ) {
			next.setText("Finish");
		} else {
			next.setText("Next");
		}
		if(currentPos == 0) {
			prev.setEnabled(false);
		}else {
			prev.setEnabled(true);
		}
	}
}
