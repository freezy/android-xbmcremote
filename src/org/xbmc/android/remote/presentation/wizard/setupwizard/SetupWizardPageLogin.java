package org.xbmc.android.remote.presentation.wizard.setupwizard;

import java.net.HttpURLConnection;

import org.apache.http.HttpException;
import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.wizard.Wizard;
import org.xbmc.android.remote.presentation.wizard.WizardPage;
import org.xbmc.android.util.ClientFactory;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SetupWizardPageLogin extends WizardPage<Host> {

	private EditText username;
	private EditText password;
	private Handler mHandler;
	private TextView errorMsg;

	public SetupWizardPageLogin(Context context, AttributeSet attrs,
			int defStyle, Wizard<Host> wizard) {
		super(context, attrs, defStyle, wizard);
	}

	public SetupWizardPageLogin(Context context, AttributeSet attrs,
			Wizard<Host> wizard) {
		super(context, attrs, wizard);
	}

	public SetupWizardPageLogin(Context context, Wizard<Host> wizard) {
		super(context, wizard);
	}

	@Override
	public WizardPage<Host> getNextPage() {
		return null;
	}

	@Override
	public int getLayoutId() {
		return R.layout.setup_page_login;
	}

	@Override
	protected void onInit() {
		mHandler = new Handler();
		username = (EditText)findViewById(R.id.setup_wizard_username);
		password = (EditText)findViewById(R.id.setup_wizard_password);
		errorMsg = (TextView)findViewById(R.id.setup_page_login_msg);
		setCanFinish(true);
	}
	
	@Override
	public OnClickListener getNextClickListener() {
		return new OnClickListener() {
			public void onClick(View v) {
				getInput().user = username.getText().toString();
				getInput().pass = password.getText().toString();
				ClientFactory.resetClient(getInput());
				testConnection();
			}
		};
	}

	private void testConnection() {
		IInfoManager info = ManagerFactory.getInfoManager(new INotifiableController() {
			public void runOnUI(Runnable action) {
				mHandler.post(action);
			}
			
			public void onWrongConnectionState(int state, INotifiableManager manager,
					Command<?> source) {
			}
			
			public void onMessage(String message) {
			}
			
			public void onError(Exception e) {
				if (e instanceof HttpException
						&& e.getMessage()
						.equals(HttpURLConnection.HTTP_UNAUTHORIZED)) {
					errorMsg.setText("Username or password is wrong.\nPlease check both and try again.");
				}
			}
		});
		info.getSystemInfo(new DataResponse<String>() {
			@Override
			public void run() {
				if(value != null && !value.equals("")) {
					//ok, finally we got it, we can login into xbmc
					showNextPage();
				}
			}
		}, SystemInfo.SYSTEM_BUILD_VERSION, getContext());
	}
}
