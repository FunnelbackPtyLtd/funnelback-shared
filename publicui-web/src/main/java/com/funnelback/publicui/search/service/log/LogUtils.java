package com.funnelback.publicui.search.service.log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletRequest;

import lombok.extern.log4j.Log4j;

import org.apache.commons.codec.digest.DigestUtils;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.log.Log;

@Log4j
public class LogUtils {
	
	/**
	 * Transforms a address into a search identifier depending of the type of identifier needed.
	 * @param data The source address
	 * @param idType Type of identifier needed
	 * @return The transformed address
	 */
	public static String getUserIdentifier(ServletRequest request, DefaultValues.UserIdToLog idType) {
		if (request == null) {
			return Log.USERID_NOTHING;
		}
		
		try {
			switch(idType) {
			case ip_hash:
				return DigestUtils.md5Hex(InetAddress.getByName(request.getRemoteAddr()).getHostAddress());
			case nothing:
				return Log.USERID_NOTHING;
			case ip:
			default:
				return InetAddress.getByName(request.getRemoteAddr()).getHostAddress();
			}
		} catch (UnknownHostException uhe) {
			log.warn("Unable to get a user id from adress '"+request.getRemoteAddr()+"', for mode '" + idType + "'", uhe);
			return Log.USERID_NOTHING;
		}
	}
	
}
