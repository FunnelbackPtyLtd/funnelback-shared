package com.funnelback.publicui.web.views.form.soy;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.javasrc.restricted.JavaExpr;
import com.google.template.soy.javasrc.restricted.SoyJavaSrcFunctionUtils;
import com.google.template.soy.javasrc.restricted.SoyJavaSrcPrintDirective;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcPrintDirective;
import com.google.template.soy.tofu.restricted.SoyTofuFunctionUtils;
import com.google.template.soy.tofu.restricted.SoyTofuPrintDirective;

@lombok.extern.apachecommons.Log
public class ChangeOrAddQsParamDirective implements SoyTofuPrintDirective, SoyJsSrcPrintDirective, SoyJavaSrcPrintDirective {

	@Inject
	public ChangeOrAddQsParamDirective() {}
	
	@Override
	public String getName() {
		return "|changeOrAddQsParam";
	}

	@Override
	public Set<Integer> getValidArgsSizes() {
		return ImmutableSet.of(2);
	}

	@Override
	public boolean shouldCancelAutoescape() {
		return false;
	}

	@Override
	public JavaExpr applyForJavaSrc(JavaExpr qs, List<JavaExpr> args) {
		// JavaExpr qs = args.get(0);
		JavaExpr paramName = args.get(0);
		JavaExpr newValue = args.get(0);
		
		Pattern p = Pattern.compile("[&\\?]" + paramName.getText() + "=[^&]*");
		Matcher m = p.matcher(qs.getText());
		if (m.find()) {
			return SoyJavaSrcFunctionUtils.toStringJavaExpr(m.replaceAll(newValue.getText()));
		} else {
			return SoyJavaSrcFunctionUtils.toStringJavaExpr(qs.getText() + "&amp;" + paramName.getText() + "=" + newValue.getText()); 
		}
	}

	@Override
	public JsExpr applyForJsSrc(JsExpr qs, List<JsExpr> args) {
		JsExpr paramName = args.get(0);
		JsExpr newValue = args.get(1);
		
		String re = "'[&\\?;]' + "+paramName.getText()+" + '=[^&]*'";
		StringBuffer out = new StringBuffer();
		out.append("(").append(qs.getText()).append(".search(").append(re).append(") > -1) ? ");
		out.append(qs.getText()).append(".replace(").append(re).append(", ").append(paramName.getText() + " + '=' + " + newValue.getText()).append(")");
		out.append(" : ");
		out.append(qs.getText()).append("+'&amp;' + ").append(paramName.getText()).append("+ '=' +").append(newValue.getText());
				
		return new JsExpr(out.toString(), Integer.MAX_VALUE);
	}

	@Override
	public String applyForTofu(String qs, List<SoyData> args) {
		SoyData paramName = args.get(0);
		SoyData newValue = args.get(1);
		
		Pattern p = Pattern.compile("[&?]" + paramName.stringValue() + "=[^&]*");
		Matcher m = p.matcher(qs);
		if (m.find()) {
			return m.replaceAll(newValue.stringValue());
		} else {
			return qs + "&amp;" + paramName.stringValue() + "=" + newValue.stringValue(); 
		}
	}

}
