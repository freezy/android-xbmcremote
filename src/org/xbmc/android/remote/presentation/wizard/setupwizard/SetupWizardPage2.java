package org.xbmc.android.remote.presentation.wizard.setupwizard;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.wizard.Wizard;
import org.xbmc.android.remote.presentation.wizard.WizardPage;
import org.xbmc.android.util.ClientFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.info.GuiSettings;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class SetupWizardPage2 extends WizardPage<Host> {

	private IInfoManager info;
	private Handler mHandler;
	private TextView port;
	private CheckBox enableES;

	private boolean esEnabled = false;
	private boolean esAllEnabled = false;
	private IControlManager control;

	public SetupWizardPage2(Context context, AttributeSet attrs, int defStyle, Wizard<Host> wizard) {
		super(context, attrs, defStyle, wizard);
	}

	public SetupWizardPage2(Context context, AttributeSet attrs, Wizard<Host> wizard) {
		super(context, attrs, wizard);
	}

	public SetupWizardPage2(Context context, Wizard<Host> wizard) {
		super(context, wizard);
	}

	protected void onInit() {
		mHandler = new Handler();
		port = (TextView) findViewById(R.id.setup_wizard_es_port);
		enableES = (CheckBox) findViewById(R.id.setup_wizard_enable_es);
	}
	
	@Override
	public void show() {
		super.show();
		showBusyMessage(getContext().getString(R.string.setup_wizard_checking_es_wait));
		ClientFactory.resetClient(getInput());
		info = ManagerFactory.getInfoManager(new INotifiableController() {

			public void runOnUI(Runnable action) {
				mHandler.post(action);
			}

			public void onWrongConnectionState(int state,
					INotifiableManager manager, Command<?> source) {
			}

			public void onMessage(String message) {
			}

			public void onError(Exception e) {

			}
		});
		control = ManagerFactory.getControlManager(new INotifiableController() {
			public void runOnUI(Runnable action) {
				mHandler.post(action);
			}

			public void onWrongConnectionState(int state,
					INotifiableManager manager, Command<?> source) {
			}

			public void onMessage(String message) {
			}

			public void onError(Exception e) {
			}
		});
		info.getGuiSettingBool(new DataResponse<Boolean>() {
			@Override
			public void run() {
				esEnabled = value.booleanValue();
				checkEnabledAll();
			}
		}, GuiSettings.Services.EVENTSERVER_ENABLED, getContext());
	}

	private void checkEnabledAll() {
		info.getGuiSettingBool(new DataResponse<Boolean>() {
			public void run() {
				esAllEnabled = value.booleanValue();
				getPort();
			}
		}, GuiSettings.Services.EVENTSERVER_ENABLED_ALL, getContext());
	}

	private void getPort() {
		info.getGuiSettingInt(new DataResponse<Integer>() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					public void run() {
						if(value > 0)
							port.setText(Integer.toString(value));
						removeBusyMessage();
						enableES.setChecked(esAllEnabled);
						enableES.setEnabled(!esAllEnabled);
						port.setEnabled(!esAllEnabled);
						getInput().esPort = Integer.valueOf(port.getText().toString());
						setCanFinish(esEnabled && esAllEnabled);
						enableES.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								enableES.setEnabled(false);
								showBusyMessage(getContext().getString(
										R.string.setup_wizard_enable_es_wait));
								enableES();
							}
						});
					}
				});
			}
		}, GuiSettings.Services.EVENTSERVER_PORT, getContext());
	}

	private void enableES() {
		if (!esEnabled)
			control.setGuiSetting(new DataResponse<Boolean>() {
				@Override
				public void run() {
					enableEsAll();
				}
			}, GuiSettings.Services.EVENTSERVER_ENABLED, "true", getContext());
		else
			enableEsAll();
	}

	private void enableEsAll() {
		control.setGuiSetting(new DataResponse<Boolean>() {
			@Override
			public void run() {
				enableES.setChecked(value);
				enableES.setEnabled(!value);
				port.setEnabled(!value);
				setCanFinish(value);
				removeBusyMessage();
			}
		}, GuiSettings.Services.EVENTSERVER_ENABLED_ALL, "true", getContext());
	}

	@Override
	public WizardPage getNextPage() {
		return null;
	}

	@Override
	public int getLayoutId() {
		return R.layout.setup_page_2;
	}
}
