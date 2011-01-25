package com.funnelback.publicui.aop;

import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Resource;

import lombok.extern.apachecommons.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Aspect for profiling information
 */
@Aspect
@Component
@Log
public class ProfiledAspect {

	@Value("#{appProperties['profiling.history.size']}")
	private int historySize;
	
	@Resource(name="profilingStats")
	private Map<String, LinkedList<Long>> stats;

	@Pointcut("within(com.funnelback.publicui..*) && execution(* *(..)) && @annotation(com.funnelback.publicui.aop.Profiled)")
	public void profiledAnnotatedMethod() {}

	@Pointcut("within(com.funnelback.publicui.web.controllers.SearchController) && execution(public * search(..))")
	public void manuallyProfiledMethod() {
	}

	@Around("manuallyProfiledMethod()")
	public Object adviceManual(ProceedingJoinPoint pjp) throws Throwable {
		return profile(pjp);
	}
	
	@Around("profiledAnnotatedMethod()")
	public Object adviceAnnotated(ProceedingJoinPoint pjp) throws Throwable {
		return profile(pjp);
	}
	
	private Object profile(ProceedingJoinPoint pjp) throws Throwable {
		StopWatch sw = new StopWatch(pjp.getTarget().getClass().getName());
		try {
			sw.start(pjp.getSignature().getName());
			return pjp.proceed();
		} finally {
			sw.stop();
			String key = pjp.getTarget().getClass().getName() + "." + pjp.getSignature().getName();
			LinkedList<Long> methodStats = stats.get(key);
			if ( methodStats == null) {
				methodStats = new LinkedList<Long>();
				stats.put(key, methodStats);
				
			}
			methodStats.addLast(sw.getTotalTimeMillis());
			if (methodStats.size() > historySize) {
				methodStats.removeFirst();
			}
		}
	}

}
