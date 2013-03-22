package com.funnelback.publicui.utils.jna;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.log4j.Log4j;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;

/**
 * {@link InputStream} that reads a file using Native Windows APIs
 * through JNA.
 *
 */
@Log4j
public class WindowsFileInputStream extends WindowsHandleInputStream {

    /**
     * Creates a new instance and opens the file.
     * 
     * @param path File path to read
     * @throws IOException If something goes wrong when opening the file
     */
    public WindowsFileInputStream(String path) throws IOException {
        super(Kernel32.INSTANCE.CreateFile(
                path,
                WinNT.FILE_READ_DATA,
                WinNT.FILE_SHARE_READ,
                null,
                WinNT.OPEN_EXISTING,
                WinNT.FILE_ATTRIBUTE_NORMAL,
                null));
        
        if (handle.equals(WinBase.INVALID_HANDLE_VALUE)) {
            int errno = Kernel32.INSTANCE.GetLastError();
            
            Win32Exception ex = new Win32Exception(errno);
            log.error("Unable to open file '"+path+"'", ex);
            
            switch (errno) {
            case Kernel32.ERROR_FILE_NOT_FOUND:
            case Kernel32.ERROR_PATH_NOT_FOUND:  
                throw new FileNotFoundException(path + ": " + ex.getMessage());
            case Kernel32.ERROR_ACCESS_DENIED:
                throw new AccessDeniedException(path, ex);
            default:
                throw new IOException("Could not open file '"+path+"' for reading", new Win32Exception(errno));
            }
        }

    }
    
    /**
     * Used when access to a file is denied.
     */
    public class AccessDeniedException extends IOException {

        private static final long serialVersionUID = 1L;

        /**
         * @param message Message to use
         * @param cause Original cause of the error
         */
        public AccessDeniedException(String message, Throwable cause) {
            super(message, cause);
        }
        
    }

}
