package com.funnelback.publicui.web.views.form.js;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.ringojs.wrappers.ScriptableList;
import org.ringojs.wrappers.ScriptableMap;

public class CustomWrapFactory extends WrapFactory {
	
	public CustomWrapFactory() {
		setJavaPrimitiveWrap(false);
	}
	
	@Override
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
		if (javaObject instanceof Map) {
			return new ScriptableMap(scope, (Map<?, ?>) javaObject);
		} else if (javaObject instanceof List) {
			return new ScriptableList(scope, (List<Object>) javaObject);
		} else {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
	}
}
