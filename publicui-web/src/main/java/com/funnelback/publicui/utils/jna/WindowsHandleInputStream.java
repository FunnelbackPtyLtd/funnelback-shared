package com.funnelback.publicui.utils.jna;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import lombok.RequiredArgsConstructor;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

/**
 * {@link InputStream} that wraps a Windows {@link HANDLE}
 * through JNA
 */
@RequiredArgsConstructor
public class WindowsHandleInputStream extends InputStream {

    private static final short FF = 0xFF;
    
    /** HANDLE to read */
    protected final HANDLE handle;
    
    /** Temporary buffer used to read content */
    private final ByteBuffer buf = ByteBuffer.allocate(1);
    
    /** Nb of byte read */
    private final IntByReference nbRead = new IntByReference();

    
    @Override
    public int read() throws IOException {
        
        buf.rewind();
        nbRead.setValue(0);
        boolean success = Kernel32.INSTANCE.ReadFile(handle, buf, buf.capacity(), nbRead, null);
        if (!success) {
            Exception ex = new Win32Exception(Kernel32.INSTANCE.GetLastError());
            ex.printStackTrace();
            throw new IOException(ex);
        } else if (nbRead.getValue() <= 0) {
            return -1;
        } else {
            return buf.get(0) & FF;   // Mask it with 0xFF to get a number in the 0-255 range
        }
    }

    @Override
    public void close() throws IOException {
        Kernel32.INSTANCE.CloseHandle(handle);
    }

}
