package com.funnelback.publicui.aop;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import lombok.Getter;
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

	@Resource(name = "profilingStatsNotSynchronized")
	private Map<String, MethodStats> stats;

	@Pointcut("within(com.funnelback.publicui..*) && execution(* *(..)) && @annotation(com.funnelback.publicui.aop.Profiled)")
	public void profiledAnnotatedMethod() {
	}

	@Pointcut("within(com.funnelback.publicui.search..*)")
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

			MethodStats methodStats = stats.get(key);
			if (methodStats == null) {
				methodStats = new MethodStats(historySize);
				stats.put(key, methodStats);
			}
			methodStats.addValue(new Date(), sw.getTotalTimeMillis());
		}
	}
	
	public class MethodStats {
		@Getter private Date[] dates = new Date[historySize];
		@Getter private long[] values = new long[historySize];
		@Getter private int count = 0;
		private int size = 0;
		
		public MethodStats(int size) {
			dates = new Date[size];
			values = new long[size];
			count = 0;
			this.size = size;
		}
		
		public synchronized void addValue(Date when, long value) {
			if (count >= size) {
				// Shift array to the left
				System.arraycopy(dates, 1, dates, 0, dates.length-1);
				System.arraycopy(values, 1, values, 0, values.length-1);
				count--;
			}
			dates[count] = when;
			values[count] = value;
			count++;
		}
		
		public long getPeakValue() {
			long peak = 0;
			for(long value: values) {
				if (value > peak) {
					peak = value;
				}
			}
			
			return peak;
		}
	}

}
