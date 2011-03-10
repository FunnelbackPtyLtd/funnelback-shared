package com.funnelback.publicui.web.views.form;

import lombok.Getter;
import lombok.Setter;

import org.springframework.web.servlet.view.AbstractUrlBasedView;

public abstract class AbstractCollectionFormView extends AbstractUrlBasedView {

	@Getter @Setter String templateContent;

}
