package com.funnelback.publicui.log;

import java.net.InetAddress;

import org.apache.commons.codec.digest.DigestUtils;

import com.funnelback.common.config.DefaultValues;

public class LogUtils {

	public static final String USERID_NOTHING = "-";
	
	/**
	 * Transforms a address into a search identifier depending of the type of identifier needed.
	 * @param data The source address
	 * @param idType Type of identifier needed
	 * @return The transformed address
	 */
	public static String getUserIdentifier(InetAddress addr, DefaultValues.UserIdToLog idType) {
		switch(idType) {
		case ip_hash:
			return DigestUtils.md5Hex(addr.getHostAddress());
		case nothing:
			return USERID_NOTHING;
		case ip:
		default:
			return addr.getHostAddress();
		}
	}
	
}
