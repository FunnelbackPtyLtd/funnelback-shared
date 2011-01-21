package com.funnelback.publicui.search.lifecycle.data.fetcher.padre.exec;

import java.nio.ByteBuffer;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.search.lifecycle.data.fetcher.padre.PadreForking;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.platform.win32.Secur32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Forks PADRE using Windows Native calls, in order to use impersonation.
 */
@Log
public class WindowsNativePadreForker implements PadreForker {

	/**
	 * Size of the buffer used to read PADRE stdout
	 */
	private static final int STDOUT_BUFFER_SIZE = 4096;
	
	/**
	 * How long to wait, in ms, for PADRE to finish
	 */
	public int waitTimeout;
	
	public WindowsNativePadreForker(int waitTimeout) {
		this.waitTimeout = waitTimeout;
	}
	
	@Override
	public String execute(String commandLine, Map<String, String> environmnent) throws PadreForkingException {

		if (log.isDebugEnabled()) {
			log.debug("Native user name is '" + Secur32Util.getUserNameEx(Secur32.EXTENDED_NAME_FORMAT.NameSamCompatible) + "'");
		}
		
		String result = null;
		HANDLEByReference hToken = new HANDLEByReference(WinBase.INVALID_HANDLE_VALUE);
		HANDLEByReference primaryToken = new HANDLEByReference(WinBase.INVALID_HANDLE_VALUE);
		try {
			// Opening current thread token
			if (! Advapi32.INSTANCE.OpenThreadToken(
					Kernel32.INSTANCE.GetCurrentThread(),
					WinNT.TOKEN_ALL_ACCESS,
					true,
					hToken)) {
				throw new PadreForkingException("Call to OpenThreadToken() failed", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
			
			// Duplicate token in order to obtain a primary token
			// required to create a new process
			if ( ! Advapi32.INSTANCE.DuplicateTokenEx(
					hToken.getValue(),
					WinNT.TOKEN_ALL_ACCESS,
					null,
					WinNT.SECURITY_IMPERSONATION_LEVEL.SecurityDelegation,
					WinNT.TOKEN_TYPE.TokenPrimary,
					primaryToken)) {
				throw new PadreForkingException("Call to DuplicateTokenEx() failed", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
	
			// Create Pipes for STDOUT
			WinBase.SECURITY_ATTRIBUTES saPipes = new WinBase.SECURITY_ATTRIBUTES();
			saPipes.bInheritHandle = true;
			saPipes.lpSecurityDescriptor = null;
		
			HANDLEByReference hChildOutRead = new HANDLEByReference();
			HANDLEByReference hChildOutWrite = new HANDLEByReference();
			if ( ! Kernel32.INSTANCE.CreatePipe(hChildOutRead, hChildOutWrite, saPipes, 0) ) {
				throw new PadreForkingException("Unable to create child stdout pipe", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
			if (! Kernel32.INSTANCE.SetHandleInformation(hChildOutRead.getValue(), WinBase.HANDLE_FLAG_INHERIT, 0)) {
				throw new PadreForkingException("Unable to set child stdout pipe info", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
			
			WinBase.STARTUPINFO si = new WinBase.STARTUPINFO();
			final WinBase.PROCESS_INFORMATION pi = new WinBase.PROCESS_INFORMATION();
			si.hStdError = hChildOutWrite.getValue();
			si.hStdOutput = hChildOutWrite.getValue();
			si.dwFlags = WinBase.STARTF_USESTDHANDLES;
						
			// Actually fork
			log.debug("Calling CreateProcessAsUser() for command line '" + commandLine + "' with environment '" + environmnent + "'");
			if ( ! Advapi32.INSTANCE.CreateProcessAsUser(
					primaryToken.getValue(),
					null,
					commandLine,
					null,
					null,
					true,
					WinBase.CREATE_UNICODE_ENVIRONMENT | WinBase.CREATE_NO_WINDOW,
					Advapi32Util.getEnvironmentBlock(environmnent),
					null,
					si,
					pi)) {
				throw new PadreForkingException("Call to CreateProcessAsUser() failed", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
			log.debug("Created process, pid is " + pi.dwProcessId + ", threadid is " + pi.dwThreadId);
			
			// Read PADRE stdout
			result = readFullStdOut(hChildOutWrite.getValue(), hChildOutRead.getValue());
			
			int state = Kernel32.INSTANCE.WaitForSingleObject(pi.hProcess, waitTimeout);
			if (state != WinBase.WAIT_OBJECT_0) {
				log.warn("PADRE did not return in '" + waitTimeout + "ms. "
						+ "WaitForSingleObject() returned " + state + ", LastError is " + Kernel32.INSTANCE.GetLastError() + ". "
						+ "The process will be terminated");
				Kernel32.INSTANCE.TerminateProcess(pi.hProcess, -1);
			}

			IntByReference rc = new IntByReference();
			if( ! Kernel32.INSTANCE.GetExitCodeProcess(pi.hProcess, rc)) {
				throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
			}
			if (rc.getValue() != PadreForking.RC_SUCCESS) {
				log.error("PADRE didn't exit successfully. Return code was: " + rc.getValue());
				throw new PadreForkingException(rc.getValue(), result);
			}
			log.debug("PADRE exited successfuly");
		} finally {
			if(! hToken.getValue().equals(WinBase.INVALID_HANDLE_VALUE)) {
				Kernel32.INSTANCE.CloseHandle(hToken.getValue());
			}
			if(! primaryToken.getValue().equals(WinBase.INVALID_HANDLE_VALUE)) {
				Kernel32.INSTANCE.CloseHandle(primaryToken.getValue());
			}
		}
		
		if (log.isTraceEnabled()) {
			log.trace("PADRE output is: '" + result + "'");
		}
		return result;
	}

	/**
	 * Reads STDOUT from PADRE
	 * @param hChildOutWrite Pipe to write to STDOUT. Will be closed.
	 * @param hChildOutRead Pipe to read from STDOUT. Will be closed.
	 * @return Content of STDOUT
	 */
	private String readFullStdOut(HANDLE hChildOutWrite, HANDLE hChildOutRead) {
		StringBuffer out = new StringBuffer();
		if ( !Kernel32.INSTANCE.CloseHandle(hChildOutWrite)) {
			log.warn("Unable to close the stdout write pipe of child process (GetLastError="+Kernel32.INSTANCE.GetLastError()+ ")");
		}

		ByteBuffer buf = ByteBuffer.allocate(STDOUT_BUFFER_SIZE);
		IntByReference nbRead = new IntByReference();
		while(true) {
			boolean success = Kernel32.INSTANCE.ReadFile(hChildOutRead, buf, buf.capacity(), nbRead, null);
			if ( ! success || nbRead.getValue() == 0) {
				break;
			} else {
				byte[] b = new byte[nbRead.getValue()];
				buf.get(b, 0, nbRead.getValue());
				
				out.append(Native.toString(b));
				buf.clear();
			}
		}
		
		if (! Kernel32.INSTANCE.CloseHandle(hChildOutRead)) {
			log.warn("Unable to close stdout read pipe of child process (GetLastError="+Kernel32.INSTANCE.GetLastError()+")");
		}
		return out.toString();
	}

	
}
