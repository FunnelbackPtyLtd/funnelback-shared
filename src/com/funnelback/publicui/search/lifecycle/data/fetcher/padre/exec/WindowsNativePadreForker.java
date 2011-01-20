package com.funnelback.publicui.search.lifecycle.data.fetcher.padre.exec;

import java.nio.ByteBuffer;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import com.funnelback.common.utils.Wait;
import com.funnelback.publicui.search.lifecycle.data.fetcher.padre.PadreForking;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinNT.LUID;
import com.sun.jna.ptr.IntByReference;

/**
 * Forks PADRE using Windows Native calls, in order to use impersonation.
 */
@Log
public class WindowsNativePadreForker implements PadreForker {

	/**
	 * Name of the environment variable containing the system root
	 * under windows.
	 */
	private static final String SYSTEMROOT_ENV = "SystemRoot";
	
	/**
	 * Size of the buffer used to read PADRE stdout
	 */
	private static final int STDOUT_BUFFER_SIZE = 4096;
	
	/**
	 * Default interval, in ms, used call the GetExitCodeProcess() when
	 * waiting for PADRE to finish
	 */
	private static final int DEFAULT_WAIT_INTERVAL = 50;
	
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
			log.debug("Native user name is '" + Advapi32Util.getUserName() + "'");
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
			
			String[] privileges = {WinNT.SE_CREATE_TOKEN_NAME, 
					WinNT.SE_ASSIGNPRIMARYTOKEN_NAME, 
					WinNT.SE_LOCK_MEMORY_NAME, 
					WinNT.SE_INCREASE_QUOTA_NAME, 
					// WinNT.SE_UNSOLICITED_INPUT_NAME, 
					WinNT.SE_MACHINE_ACCOUNT_NAME, 
					WinNT.SE_TCB_NAME, 
					WinNT.SE_SECURITY_NAME, 
					WinNT.SE_TAKE_OWNERSHIP_NAME, 
					WinNT.SE_LOAD_DRIVER_NAME, 
					WinNT.SE_SYSTEM_PROFILE_NAME, 
					WinNT.SE_SYSTEMTIME_NAME, 
					WinNT.SE_PROF_SINGLE_PROCESS_NAME, 
					WinNT.SE_INC_BASE_PRIORITY_NAME, 
					WinNT.SE_CREATE_PAGEFILE_NAME, 
					WinNT.SE_CREATE_PERMANENT_NAME, 
					WinNT.SE_BACKUP_NAME, 
					WinNT.SE_RESTORE_NAME, 
					WinNT.SE_SHUTDOWN_NAME, 
					WinNT.SE_DEBUG_NAME, 
					WinNT.SE_AUDIT_NAME, 
					WinNT.SE_SYSTEM_ENVIRONMENT_NAME, 
					WinNT.SE_CHANGE_NOTIFY_NAME, 
					WinNT.SE_REMOTE_SHUTDOWN_NAME, 
					WinNT.SE_UNDOCK_NAME, 
					WinNT.SE_SYNC_AGENT_NAME, 
					WinNT.SE_ENABLE_DELEGATION_NAME, 
					WinNT.SE_MANAGE_VOLUME_NAME, 
					WinNT.SE_IMPERSONATE_NAME, 
					WinNT.SE_CREATE_GLOBAL_NAME};
			
			WinNT.TOKEN_PRIVILEGES tp = new WinNT.TOKEN_PRIVILEGES(privileges.length);
			int i=0;
			for (String privilege : privileges) {
				// Lookup privilege
				LUID luid = new LUID();
				if ( ! Advapi32.INSTANCE.LookupPrivilegeValue(null, privilege, luid)) {
					throw new PadreForkingException("Call to LookupPrivilegeValue() failed for privilege '" + privilege + "'", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
				}
				
				tp.Privileges[i++] = new WinNT.LUID_AND_ATTRIBUTES(luid, new DWORD(WinNT.SE_PRIVILEGE_ENABLED));
			}
			
			if (! Advapi32.INSTANCE.AdjustTokenPrivileges(primaryToken.getValue(), false, tp, 0, null, null)) {
				throw new PadreForkingException("Call to AdjustTokenPrivileges() failed", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
	
			// Create Pipes for STDOUT/IN/ERR
			WinBase.SECURITY_ATTRIBUTES saPipes = new WinBase.SECURITY_ATTRIBUTES();
			saPipes.bInheritHandle = true;
			saPipes.lpSecurityDescriptor = null;
		
			HANDLEByReference hChildOutRead = new HANDLEByReference();
			HANDLEByReference hChildOutWrite = new HANDLEByReference();
	//		HANDLEByReference hChildInRead = new HANDLEByReference();
	//		HANDLEByReference hChildInWrite = new HANDLEByReference();
			if ( ! Kernel32.INSTANCE.CreatePipe(hChildOutRead, hChildOutWrite, saPipes, 0) ) {
				throw new PadreForkingException("Unable to create child stdout pipe", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
			if (! Kernel32.INSTANCE.SetHandleInformation(hChildOutRead.getValue(), WinBase.HANDLE_FLAG_INHERIT, 0)) {
				throw new PadreForkingException("Unable to set child stdout pipe info", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
			}
	//		if ( ! Kernel32.INSTANCE.CreatePipe(hChildInRead, hChildInWrite, saPipes, 0)) {
	//			throw new PadreForkingException("Unable to create child stdin pipe", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
	//		}
	//		if (! Kernel32.INSTANCE.SetHandleInformation(hChildInWrite.getValue(), WinBase.HANDLE_FLAG_INHERIT, 0)) {
	//			throw new PadreForkingException("Unable to set child stdin pipe info", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
	//		}
			
			WinBase.STARTUPINFO si = new WinBase.STARTUPINFO();
			final WinBase.PROCESS_INFORMATION pi = new WinBase.PROCESS_INFORMATION();
			si.hStdError = hChildOutWrite.getValue();
			si.hStdOutput = hChildOutWrite.getValue();
	//		si.hStdInput = hChildInRead.getValue();
			si.dwFlags = WinBase.STARTF_USESTDHANDLES;
			
			// SystemRoot environment variable is MANDATORY for TRIM DLS checks
			// The TRIM SDK uses WinSock to connect to the remote server, and 
			// WinSock needs SystemRoot to initialize itself.
			environmnent.put(SYSTEMROOT_ENV, System.getenv(SYSTEMROOT_ENV));
			
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
	
			// Read PADRE stdout
			result = readFullStdOut(hChildOutWrite.getValue(), hChildOutRead.getValue());
			
			// Wait for PADRE to finish
			// TODO use Kernel32.INSTANCE.WaitForSingleObject(hHandle, dwMilliseconds);
			final IntByReference rc = new IntByReference();
			try {
				new Wait() {
					@Override
					public boolean until() {
						if( ! Kernel32.INSTANCE.GetExitCodeProcess(pi.hProcess, rc)) {
							throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
						}
						return rc.getValue() != WinBase.STILL_ACTIVE;
					}
				}.wait(waitTimeout, DEFAULT_WAIT_INTERVAL , "PADRE to finish");
			} catch (Exception e) {
				log.warn("Waiting for PADRE to finish failed. Trying to terminate the process cleanly.", e);
				// Something went wrong, either we timeouted or were unable to retrieve
				// the exit code. Try to terminate the process cleanly
				Kernel32.INSTANCE.TerminateProcess(pi.hProcess, -1);

				if (rc.getValue() != PadreForking.RC_SUCCESS) {
					log.debug("PADRE return code: " + rc.getValue());
					throw new PadreForkingException(rc.getValue(), result);
				}
			}
		} finally {
			if(! hToken.getValue().equals(WinBase.INVALID_HANDLE_VALUE)) {
				Kernel32.INSTANCE.CloseHandle(hToken.getValue());
			}
			if(! primaryToken.getValue().equals(WinBase.INVALID_HANDLE_VALUE)) {
				Kernel32.INSTANCE.CloseHandle(primaryToken.getValue());
			}
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
