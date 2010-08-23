package org.xbmc.android.remote.presentation.wizard.setupwizard;

import org.xbmc.android.remote.presentation.wizard.Wizard;
import org.xbmc.android.remote.presentation.wizard.WizardPage;
import org.xbmc.api.object.Host;

public class SetupWizard extends Wizard<Host> {

	@Override
	public void doSetupPages() {
		final Host host = new Host();
		WizardPage<Host> page = new SetupWizardPage1(this, this);
		page.setInput(host);
		addPage(page);
		addPage(new SetupWizardPage2(this, this));
	}

}
