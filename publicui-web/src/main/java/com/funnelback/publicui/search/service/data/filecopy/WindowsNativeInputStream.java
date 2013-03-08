package com.funnelback.publicui.search.service.data.filecopy;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import lombok.extern.log4j.Log4j;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

/**
 * {@link InputStream} that reads a file using Native Windows APIs
 * through JNA.
 *
 */
@Log4j
public class WindowsNativeInputStream extends InputStream implements Closeable {

    private static final short FF = 0xFF;
    
    private final HANDLE hFile;
    private final ByteBuffer buf = ByteBuffer.allocate(1);
    private final IntByReference nbRead = new IntByReference();

    /**
     * Creates a new instance and opens the file.
     * 
     * @param path File path to read
     * @throws IOException If something goes wrong when opening the file
     */
    public WindowsNativeInputStream(String path) throws IOException {
        hFile = Kernel32.INSTANCE.CreateFile(
                path,
                WinNT.FILE_READ_DATA,
                WinNT.FILE_SHARE_READ,
                null,
                WinNT.OPEN_EXISTING,
                WinNT.FILE_ATTRIBUTE_NORMAL,
                null);
        
        if (hFile.equals(WinBase.INVALID_HANDLE_VALUE)) {
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
    
    @Override
    public int read() throws IOException {
        
        buf.rewind();
        nbRead.setValue(0);
        boolean success = Kernel32.INSTANCE.ReadFile(hFile, buf, buf.capacity(), nbRead, null);
        if (!success) {
            throw new IOException(new Win32Exception(Kernel32.INSTANCE.GetLastError()));
        } else if (nbRead.getValue() <= 0) {
            return -1;
        } else {
            return buf.get(0) & FF;   // Mask it with 0xFF to get a number in the 0-255 range
        }
    }
    
    @Override
    public void close() throws IOException {
        Kernel32.INSTANCE.CloseHandle(hFile);
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
