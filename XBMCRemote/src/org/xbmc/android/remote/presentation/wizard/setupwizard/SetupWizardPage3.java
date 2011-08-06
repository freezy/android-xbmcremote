package org.xbmc.android.remote.presentation.wizard.setupwizard;

import java.io.IOException;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.wizard.Wizard;
import org.xbmc.android.remote.presentation.wizard.WizardPage;
import org.xbmc.android.util.ClientFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SetupWizardPage3 extends WizardPage<Host> {

	private Button retry;
	private TextView msg;
	private Handler mHandler;
	private IControlManager control;
	
	private int initialVolume = -1;
	private IEventClientManager event;

	public SetupWizardPage3(Context context, AttributeSet attrs, int defStyle,
			Wizard<Host> wizard) {
		super(context, attrs, defStyle, wizard);
	}

	public SetupWizardPage3(Context context, AttributeSet attrs,
			Wizard<Host> wizard) {
		super(context, attrs, wizard);
	}

	public SetupWizardPage3(Context context, Wizard<Host> wizard) {
		super(context, wizard);
	}

	@Override
	public WizardPage<Host> getNextPage() {
		return null;
	}

	@Override
	public int getLayoutId() {
		return R.layout.setup_page_3;
	}

	@Override
	protected void onInit() {
		
		retry = (Button) findViewById(R.id.setup_page3_retry);
		msg = (TextView) findViewById(R.id.setup_page3_msg);
	}
	
	private void testEventServer() {
		showBusyMessage(getContext().getString(R.string.setup_wizard_trying_mute));
		control.getVolume(new DataResponse<Integer>() {
			@Override
			public void run() {
				initialVolume = value;
				mute();
			}
		}, getContext());
		
	}
	
	@Override
	public void show() {
		super.show();
		ClientFactory.resetClient(getInput());
		mHandler = new Handler();
		INotifiableController controller = new INotifiableController() {
			public void runOnUI(Runnable action) {
				mHandler.post(action);
			}
			public void onWrongConnectionState(int state, INotifiableManager manager,
					Command<?> source) {
			}
			public void onMessage(String message) {
			}
			public void onError(Exception e) {
			}
		};
		retry.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				msg.setText("");
				retry.setVisibility(View.INVISIBLE);
				testEventServer();
			}
		});
		control = ManagerFactory.getControlManager(controller);
		event = ManagerFactory.getEventClientManager(controller);
		testEventServer();
	}

	private void mute() {
		
		try {
			event.sendButton("R1", ButtonCodes.REMOTE_MUTE, false, true, true, (short) 0, (byte) 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		control.getVolume(new DataResponse<Integer>() {
			@Override
			public void run() {
				removeBusyMessage();
				if(initialVolume != value) {
					//it worked, we muted xbmc through the eventserver
					msg.setText(R.string.setup_wizard_final_message);
					try {
						event.sendButton("R1", ButtonCodes.REMOTE_MUTE, false, true, true, (short) 0, (byte) 0);
					} catch (IOException e) {
						e.printStackTrace();
					}
					setCanFinish(true);
				}else{
					//we couldn't mute xbmc through the eventserver, so display some help.
					retry.setVisibility(View.VISIBLE);
					msg.setText(R.string.setup_wizard_muting_failed);
				}
			}
		}, getContext());
	}
}
