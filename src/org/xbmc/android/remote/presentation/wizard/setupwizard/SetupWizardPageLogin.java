package org.xbmc.android.remote.presentation.wizard.setupwizard;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.wizard.Wizard;
import org.xbmc.android.remote.presentation.wizard.WizardPage;
import org.xbmc.api.object.Host;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetupWizardPageLogin extends WizardPage<Host> {

	private EditText username;
	private EditText password;
	private Button test;

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
		username = (EditText)findViewById(R.id.setup_wizard_username);
		password = (EditText)findViewById(R.id.setup_wizard_password);
		test = (Button)findViewById(R.id.setup_wizard_test_login);
		test.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
			}
		});
	}

}
