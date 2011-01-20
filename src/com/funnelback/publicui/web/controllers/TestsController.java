package com.funnelback.publicui.web.controllers;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinNT.SECURITY_IMPERSONATION_LEVEL;
import com.sun.jna.platform.win32.WinNT.TOKEN_TYPE;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

@Controller
@lombok.extern.apachecommons.Log
@RequestMapping({"/test", "/_/test"})
public class TestsController implements ServletContextAware {

	public class SingleDWORDStruct extends Structure {
		public DWORD value;
	}
	
	public static class TOKEN_ORIGIN extends Structure {
		public WinNT.LUID OriginatingLogonSession;
	}
	
	@RequestMapping("registry")
	public void testRegistry(HttpServletResponse response) throws IOException {
		try {
			response.getWriter().write("HKCU TRIM keys:\n");
			String[] subKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER,
					"Software\\Hewlett-Packard\\HP TRIM");
			for (String key : subKeys) {
				response.getWriter().write("  " + key + "\n");
			}
		} catch (Win32Exception we) {
			response.getWriter().write("Error accessing HKCU: " + we);
		}

		try {
			response.getWriter().write("HKLM TRIM keys:\n");
			String[] subKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE,
					"Software\\Hewlett-Packard\\HP TRIM");
			for (String key : subKeys) {
				response.getWriter().write("  " + key + "\n");
			}
		} catch (Win32Exception we) {
			response.getWriter().write("Error accessing HKCU: " + we);
		}
	}
	
	@RequestMapping("fileaccess")
	public void testFileAccess(HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.getWriter().write("You're currently impersonating: " + Advapi32Util.getUserName() + "\n");

		
		HANDLE hThread = Kernel32.INSTANCE.GetCurrentThread();
		if (hThread.equals(WinBase.INVALID_HANDLE_VALUE)) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		
		HANDLEByReference hToken = new HANDLEByReference();
		if (! Advapi32.INSTANCE.OpenThreadToken(hThread, WinNT.GENERIC_ALL, false, hToken)) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		
		SingleDWORDStruct tts = new SingleDWORDStruct();
		IntByReference returnLength = new IntByReference();
		if (! Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(),
				WinNT.TOKEN_INFORMATION_CLASS.TokenType,
				tts, tts.size(), returnLength)) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		
		response.getWriter().write("--- Token information :\n");
		
		String tokenType = "Unkown (" + tts.value + ")";
		if (tts.value.intValue() == TOKEN_TYPE.TokenPrimary) {
			tokenType = "TokenPrimary";
		} else if (tts.value.intValue() == TOKEN_TYPE.TokenImpersonation) {
			tokenType = "TokenImpersonation";
		}
		response.getWriter().write("\t + Token type: " + tokenType + "\n");

		if (! Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(),
				WinNT.TOKEN_INFORMATION_CLASS.TokenImpersonationLevel,
				tts, tts.size(), returnLength)) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		String tokenImpersonationLevel = "Unknown (" + tts.value + ")";
		switch (tts.value.intValue()) {
		case SECURITY_IMPERSONATION_LEVEL.SecurityAnonymous:
			tokenImpersonationLevel = "SecurityAnonymous";
			break;
		case SECURITY_IMPERSONATION_LEVEL.SecurityDelegation:
			tokenImpersonationLevel = "SecurityDelegation";
			break;
		case SECURITY_IMPERSONATION_LEVEL.SecurityIdentification:
			tokenImpersonationLevel = "SecurityIdentification";
			break;
		case SECURITY_IMPERSONATION_LEVEL.SecurityImpersonation:
			tokenImpersonationLevel = "SecurityImpersonation";
			break;
		}
		response.getWriter().write("\t + Token impersonation level : " + tokenImpersonationLevel + "\n");
		
		TOKEN_ORIGIN to = new TOKEN_ORIGIN();
		if (! Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(),
				WinNT.TOKEN_INFORMATION_CLASS.TokenOrigin,
				to, to.size(), returnLength)) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		response.getWriter().write("\t + Token origin : " + to + "\n");

		WinNT.TOKEN_PRIVILEGES tp = new WinNT.TOKEN_PRIVILEGES(256);
		if (! Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(),
				WinNT.TOKEN_INFORMATION_CLASS.TokenPrivileges,
				tp, tp.size(), returnLength)) {
			throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
		}
		response.getWriter().write("\t + Token privileges (" + tp.PrivilegeCount + ")\n");
		response.getWriter().write("\t (attributes: SE_PRIVILEGE_ENABLED=" + WinNT.SE_PRIVILEGE_ENABLED
				+ ", SE_PRIVILEGE_ENABLED_BY_DEFAULT=" + WinNT.SE_PRIVILEGE_ENABLED_BY_DEFAULT
				+ ", SE_PRIVILEGE_REMOVED=" + WinNT.SE_PRIVILEGE_REMOVED
				+ ")\n\n");
		for (int i=0; i<tp.PrivilegeCount.intValue(); i++) {
			char[] name = new char[256];
			IntByReference cchName = new IntByReference(name.length);
			if (! Advapi32.INSTANCE.LookupPrivilegeName(null, tp.Privileges[i].Luid, name, cchName)) {
				throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
			}
			response.getWriter().write("\t\t " + Native.toString(name) + ", attributes: " + tp.Privileges[i].Attributes + "\n");
		}		
		
		HANDLE h = Kernel32.INSTANCE.CreateFile(
				"\\\\internalfilesha\\DLS Share\\Shakespeare\\romeo_juliet\\index.html",
				WinNT.GENERIC_READ,
				0,
				null,
				WinNT.OPEN_EXISTING,
				WinNT.FILE_ATTRIBUTE_NORMAL,
				null);
		if (h == WinBase.INVALID_HANDLE_VALUE) {
			response.getWriter().write("Opening romeo_juliet failed, error: " + Kernel32.INSTANCE.GetLastError() + "\n");
		} else {
			response.getWriter().write("Opening romeo_juliet succeded\n");
			Kernel32.INSTANCE.CloseHandle(h);
		}
		
		h = Kernel32.INSTANCE.CreateFile(
				"\\\\internalfilesha\\DLS Share\\Shakespeare\\cleopatra\\index.html",
				WinNT.GENERIC_READ,
				0,
				null,
				WinNT.OPEN_EXISTING,
				WinNT.FILE_ATTRIBUTE_NORMAL,
				null);
		if (h == WinBase.INVALID_HANDLE_VALUE) {
			response.getWriter().write("Opening cleopatra failed, error: " + Kernel32.INSTANCE.GetLastError() + "\n");
		} else {
			response.getWriter().write("Opening cleopatra succeded\n");
			Kernel32.INSTANCE.CloseHandle(h);
		}

	}
	
	@RequestMapping("auth")
	public void testAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.debug(request.getUserPrincipal());

		Enumeration<String> names = request.getSession().getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			log.debug("SESSION [" + name + "] = '" + request.getSession().getAttribute(name) + "'");
		}
		log.debug("Native user name = " + Advapi32Util.getUserName());
		
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
	}
	
	public interface TRIMSdk {

	    TRIMSdk INSTANCE = (TRIMSdk) Native.loadLibrary("trimdsk", TRIMSdk.class, W32APIOptions.UNICODE_OPTIONS);
	}
}
