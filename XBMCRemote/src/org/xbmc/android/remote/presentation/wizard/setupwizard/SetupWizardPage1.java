package org.xbmc.android.remote.presentation.wizard.setupwizard;

import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpException;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.wizard.Wizard;
import org.xbmc.android.remote.presentation.wizard.WizardPage;
import org.xbmc.android.util.ClientFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.info.SystemInfo;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SetupWizardPage1 extends WizardPage<Host> {

	private EditText port;
	private EditText ip;
	private EditText name;
	private TextView errorMsg;
	private final Handler mHandler;
	private boolean needsLogin = false;

	private static final String validIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

	private static final String validHostnameRegex = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";
	
	private Pattern validHostPattern = Pattern.compile(validIpAddressRegex + "|" + validHostnameRegex);
	
	public SetupWizardPage1(Context context, AttributeSet attrs, int defStyle,
			Wizard<Host> wizard) {
		super(context, attrs, defStyle, wizard);
		mHandler = new Handler();
	}

	public SetupWizardPage1(Context context, AttributeSet attrs,
			Wizard<Host> wizard) {
		super(context, attrs, wizard);
		mHandler = new Handler();
	}

	public SetupWizardPage1(Context context, Wizard<Host> wizard) {
		super(context, wizard);
		mHandler = new Handler();
	}

	protected void onInit() {
		name = (EditText) findViewById(R.id.setup_wizard_name);
		ip = (EditText) findViewById(R.id.setup_wizard_ip);
		port = (EditText) findViewById(R.id.setup_wizard_port);
		errorMsg = (TextView) findViewById(R.id.setup_wizard_host_msg);
		// test = (Button) findViewById(R.id.setup_wizard_test_connection);
		// test.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// testConnection();
		// }
		// });
		setCanFinish(true);
	}

	private void testConnection() {
		showBusyMessage(getContext().getString(
				R.string.setup_wizard_connecting_wait));
		final Host currHost = HostFactory.host;
		getInput().name = name.getText().toString().trim();
		getInput().addr = ip.getText().toString().trim();
		getInput().port = Integer.parseInt(port.getText().toString());
		ClientFactory.resetClient(getInput());
		final IInfoManager info = ManagerFactory
				.getInfoManager(new INotifiableController() {
					public void runOnUI(Runnable action) {
						mHandler.post(action);
					}

					public void onWrongConnectionState(int state,
							INotifiableManager manager, Command<?> source) {
					}

					public void onMessage(String message) {
					}

					public void onError(final Exception e) {
						runOnUI(new Runnable() {
							public void run() {
								// setCanFinish(false);
								e.printStackTrace();
								if (e instanceof HttpException
										&& e.getMessage()
												.equals(HttpURLConnection.HTTP_UNAUTHORIZED)) {
									needsLogin = true;
									errorMsg.setText("");
									showNextPage();
								}
								ClientFactory.resetClient(currHost);
								removeBusyMessage();
								errorMsg.setText(getContext().getString(
										R.string.setup_wizard_cant_connect)
										+ e.getMessage());
							}
						});
					}
				});
		info.getSystemInfo(new DataResponse<String>() {
			@Override
			public void run() {
				if (value != null && !value.equals("")) {
					removeBusyMessage();
					ClientFactory.resetClient(currHost);
					errorMsg.setText("");
					// setCanFinish(true);
					showNextPage();
				}
			}
		}, SystemInfo.SYSTEM_BUILD_VERSION, getContext());
	}

	@Override
	public OnClickListener getNextClickListener() {
		return new OnClickListener() {
			public void onClick(View v) {
				Matcher matcher = validHostPattern.matcher(ip.getText().toString().trim());
				if(!matcher.matches()) {
					errorMsg.setText("Please enter a valid hostname or IP");
				}else {
					errorMsg.setText("");
					testConnection();
				}
			}
		};
	}

	@Override
	public WizardPage<Host> getNextPage() {
		if (needsLogin) {
			WizardPage<Host> page = new SetupWizardPageLogin(getContext(),
					wizard);
			page.init();
			return page;
		}
		return null;
	}

	@Override
	public int getLayoutId() {
		return R.layout.setup_page_1;
	}
}
