package com.funnelback.publicui.web.views.form.soy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.template.soy.shared.restricted.SoyFunction;
import com.google.template.soy.shared.restricted.SoyPrintDirective;

public class FunnelbackFunctionsModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<SoyPrintDirective> setBinder =	Multibinder.newSetBinder(binder(), SoyPrintDirective.class);
		setBinder.addBinding().to(ChangeOrAddQsParamDirective.class);

	}

}
