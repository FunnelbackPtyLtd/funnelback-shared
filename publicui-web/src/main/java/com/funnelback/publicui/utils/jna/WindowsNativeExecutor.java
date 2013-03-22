package com.funnelback.publicui.utils.jna;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.platform.win32.Secur32Util;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Executes a process using native Windows APIs
 * through JNA
 *
 */
@Log4j
@RequiredArgsConstructor
public class WindowsNativeExecutor {

    /**
     * Size of the buffer used to read stdout
     */
    private static final int STDOUT_BUFFER_SIZE = 4096;

    private final I18n i18n;
    
    /**
     * How long to wait, in ms, for the process to finish
     */
    public final int waitTimeout;
    
    /**
     * Executes the command using native Windows API on the current working directory
     * @param commandLine Command to execute
     * @param environment Environment to use, can be null
     * @return Result of the execution
     * @throws ExecutionException if something goes wrong, with a nested cause
     */
    public ExecutionReturn execute(String commandLine, Map<String, String> environment) throws ExecutionException {
        return execute(commandLine, environment, null);
    }
    
    /**
     * Executes the command using native Windows API
     * @param commandLine Command to execute
     * @param environment Environment to use, can be null
     * @param dir Current directory to use when executing the process
     * @return Result of the execution
     * @throws ExecutionException if something goes wrong, with a nested cause
     */
    public ExecutionReturn execute(String commandLine, Map<String, String> environment, File dir)
        throws ExecutionException {

        if (log.isDebugEnabled()) {
            log.debug("Native user name is '" + Secur32Util.getUserNameEx(Secur32.EXTENDED_NAME_FORMAT.NameSamCompatible) + "', "
                    + "Current Process ID is: " + Kernel32.INSTANCE.GetCurrentProcessId() + ", "
                    + "Current Thread ID is: " + Kernel32.INSTANCE.GetCurrentThreadId());
        }
        
        String result = null;
        int returnCode = -1;

        final WinBase.PROCESS_INFORMATION pi = new WinBase.PROCESS_INFORMATION();
        HANDLEByReference hToken = new HANDLEByReference(WinBase.INVALID_HANDLE_VALUE);
        HANDLEByReference hPrimaryToken = new HANDLEByReference(WinBase.INVALID_HANDLE_VALUE);
        boolean impersonateSelf = false;
        
        try {
            // Opening current thread token
            if (! Advapi32.INSTANCE.OpenThreadToken(
                    Kernel32.INSTANCE.GetCurrentThread(),
                    WinNT.TOKEN_ALL_ACCESS,
                    false,
                    hToken)) {
                
                int errno = Kernel32.INSTANCE.GetLastError();
                if (W32Errors.ERROR_NO_TOKEN == errno) {
                    // Can happen in case of extra searches: The thread is coming from
                    // an executor pool and is not impersonated. Impersonate it now
                    if (! Advapi32.INSTANCE.ImpersonateSelf(WinNT.SECURITY_IMPERSONATION_LEVEL.SecurityDelegation)) {
                        throw new ExecutionException(
                            i18n.tr("forking.native.function.failed", "ImpersonateSelf()"),
                            new Win32Exception(Kernel32.INSTANCE.GetLastError()));
                    }
                    
                    // Try to open the token again
                    if (! Advapi32.INSTANCE.OpenThreadToken(
                            Kernel32.INSTANCE.GetCurrentThread(),
                            WinNT.TOKEN_ALL_ACCESS,
                            false,
                            hToken)) {
                        throw new ExecutionException(
                            i18n.tr("forking.native.function.failed", "OpenThreadToken() after ImpersonateSelf()"),
                            new Win32Exception(Kernel32.INSTANCE.GetLastError()));
                    }
                    impersonateSelf = true;
                } else {
                    throw new ExecutionException(
                        i18n.tr("forking.native.function.failed", "OpenThreadToken()"),
                        new Win32Exception(Kernel32.INSTANCE.GetLastError()));
                }
            }
            
            WinBase.SECURITY_ATTRIBUTES sa = new WinBase.SECURITY_ATTRIBUTES();
            sa.bInheritHandle = true;
            sa.lpSecurityDescriptor = null;

            // Duplicate token in order to obtain a primary token
            // required to create a new process
            if ( ! Advapi32.INSTANCE.DuplicateTokenEx(
                    hToken.getValue(),
                    WinNT.TOKEN_ALL_ACCESS,
                    sa,
                    WinNT.SECURITY_IMPERSONATION_LEVEL.SecurityDelegation,
                    WinNT.TOKEN_TYPE.TokenPrimary,
                    hPrimaryToken)) {
                throw new ExecutionException(i18n.tr("forking.native.function.failed", "DuplicateTokenEx()"),
                    new Win32Exception(Kernel32.INSTANCE.GetLastError()));
            }
    
            // Create Pipes for STDOUT
            HANDLEByReference hChildOutRead = new HANDLEByReference();
            HANDLEByReference hChildOutWrite = new HANDLEByReference();
            if ( ! Kernel32.INSTANCE.CreatePipe(hChildOutRead, hChildOutWrite, sa, 0) ) {
                throw new ExecutionException(i18n.tr("forking.native.function.failed", "CreatePipe()"),
                    new Win32Exception(Kernel32.INSTANCE.GetLastError()));
            }
            if (! Kernel32.INSTANCE.SetHandleInformation(hChildOutRead.getValue(), WinBase.HANDLE_FLAG_INHERIT, 0)) {
                throw new ExecutionException(i18n.tr("forking.native.function.failed", "SetHandleInformation()"),
                    new Win32Exception(Kernel32.INSTANCE.GetLastError()));
            }
            
            WinBase.STARTUPINFO si = new WinBase.STARTUPINFO();
            si.hStdError = hChildOutWrite.getValue();
            si.hStdOutput = hChildOutWrite.getValue();
            si.dwFlags = WinBase.STARTF_USESTDHANDLES;

            // Actually fork
            log.debug("Calling CreateProcessAsUser() for command line '" + commandLine.trim()
                + "' with environment '" + environment + "'");
            
            if ( ! Advapi32.INSTANCE.CreateProcessAsUser(
                    hPrimaryToken.getValue(),
                    null,
                    commandLine.trim(),
                    sa,
                    sa,
                    true,
                    WinBase.CREATE_UNICODE_ENVIRONMENT | WinBase.CREATE_NO_WINDOW,
                    (environment != null) ? Advapi32Util.getEnvironmentBlock(environment) : null,
                    (dir != null) ? dir.getAbsolutePath() : null,
                    si,
                    pi)) {
                throw new ExecutionException(i18n.tr("forking.native.function.failed", "CreateProcessAsUser()"),
                    new Win32Exception(Kernel32.INSTANCE.GetLastError()));
            }
            log.trace("Created native process, pid is " + pi.dwProcessId + ", threadid is " + pi.dwThreadId);
            
            // Read stdout
            result = readFullStdOut(hChildOutWrite.getValue(), hChildOutRead.getValue());
            
            int state = Kernel32.INSTANCE.WaitForSingleObject(pi.hProcess, waitTimeout);
            if (state != WinBase.WAIT_OBJECT_0) {
                log.warn("Process did not return in '" + waitTimeout + "ms. "
                        + "WaitForSingleObject() returned " + state
                        + ", LastError is " + Kernel32.INSTANCE.GetLastError() + ". "
                        + "The process will be terminated");
                Kernel32.INSTANCE.TerminateProcess(pi.hProcess, -1);
            }

            IntByReference rc = new IntByReference(0);
            if( ! Kernel32.INSTANCE.GetExitCodeProcess(pi.hProcess, rc)) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            }
            
            returnCode = rc.getValue();
            if (returnCode < 0) {
                log.error(String.format("Process exited abnormally (Exit code: 0x%X)", returnCode));
            } else {
                log.debug("Process exited successfuly (NTSTATUS exit code: "+returnCode+")");
            }
        } catch (IOException ioe) {
            throw new ExecutionException(i18n.tr("forking.native.output.failed"), ioe);
        } finally {
            if (impersonateSelf) {
                if (! Advapi32.INSTANCE.RevertToSelf()) {
                    log.warn("Could not RevertToSelf(). LastError is: " + Kernel32.INSTANCE.GetLastError());
                }
            }
            
            // Close child thread & process handle
            if(! WinBase.INVALID_HANDLE_VALUE.equals(pi.hThread)) {
                Kernel32.INSTANCE.CloseHandle(pi.hThread);
            }
            if(! WinBase.INVALID_HANDLE_VALUE.equals(pi.hProcess)) {
                Kernel32.INSTANCE.CloseHandle(pi.hProcess);
            }
            
            // Close tokens
            if(! WinBase.INVALID_HANDLE_VALUE.equals(hToken.getValue())) {
                Kernel32.INSTANCE.CloseHandle(hToken.getValue());
            }
            if(! WinBase.INVALID_HANDLE_VALUE.equals(hPrimaryToken.getValue())) {
                Kernel32.INSTANCE.CloseHandle(hPrimaryToken.getValue());
            }
        }
        
        if (log.isTraceEnabled()) {
            log.trace("Process result is: '" + result + "'");
        }
        return new ExecutionReturn(returnCode, result);
    }

    /**
     * Reads STDOUT from the process
     * @param hChildOutWrite Pipe to write to STDOUT. Will be closed.
     * @param hChildOutRead Pipe to read from STDOUT. Will be closed.
     * @return Content of STDOUT
     * @throws IOException 
     */
    private String readFullStdOut(HANDLE hChildOutWrite, HANDLE hChildOutRead) throws IOException {
        if ( !Kernel32.INSTANCE.CloseHandle(hChildOutWrite)) {
            log.warn("Unable to close the stdout write pipe of child process",
                new Win32Exception(Kernel32.INSTANCE.GetLastError()));
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteBuffer buf = ByteBuffer.allocate(STDOUT_BUFFER_SIZE);
        IntByReference nbRead = new IntByReference();
        while(true) {
            boolean success = Kernel32.INSTANCE.ReadFile(hChildOutRead, buf, buf.capacity(), nbRead, null);
            if (! success) {
                if (nbRead.getValue() == 0) {
                    // EOF
                    break;
                } else {            
                    // Actual error
                    throw new IOException(new Win32Exception(Kernel32.INSTANCE.GetLastError()));
                }
            } else {
                byte[] b = new byte[nbRead.getValue()];
                buf.get(b, 0, nbRead.getValue());

                // FIXME FUN-5485 Padre sometimes output NUL characters
                // in debug comments. Strip them off to ensure XML will be
                // parsed correctly
                for (int i=0; i<b.length; i++) {
                    if (b[i] == '\0') {
                        b[i] = '0';
                    }
                }
                
                bos.write(b);
                buf.clear();
            }
        }
        
        if (! Kernel32.INSTANCE.CloseHandle(hChildOutRead)) {
            log.warn("Unable to close stdout read pipe of child process",
                new Win32Exception(Kernel32.INSTANCE.GetLastError()));
        }
        
        return Native.toString(bos.toByteArray());
    }

    /**
     * Generic exception when the execution fails
     */
    public static class ExecutionException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * @param message Custom message
         * @param cause Original cause
         */
        public ExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    
    
}
