package com.funnelback.publicui.search.service.data.filecopy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.file.AccessDeniedException;

import lombok.extern.log4j.Log4j;

import com.funnelback.common.utils.VFSURLUtils;
import com.funnelback.publicui.search.model.collection.Collection;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

/**
 * <p>Fetches a Filecopy document using nataive Win32 API.</p>
 * 
 * <p>Used on Windows when impersonation is in used, to enforce
 * the user's permissions.</p>
 * 
 * @since 12.4
 */
@Log4j
public class WindowsNativeFilecopyDocumentStreamer implements
		FilecopyDocumentStreamer {

	/**
	 * Size of the buffer used to read and stream the file
	 * back to the user
	 */
	public static final int READ_BUFFER_SIZE = 1024*16;

	@Override
	public void streamDocument(Collection collection, URI uri, OutputStream os)
			throws IOException {
		streamPartialDocument(collection, uri, os, 0);
	}

	@Override
	public void streamPartialDocument(Collection collection, URI uri, OutputStream os, int limit)
			throws IOException {
		
		// Convert the URI to a Windows path
		String windowsPath = VFSURLUtils.vfsUrlToSystemUrl(URLDecoder.decode(uri.toString(), "UTF-8"), true);
		
		log.trace("Converted URI '"+uri+"' to Windows path '"+windowsPath+"'");
		
		HANDLE hFile = new HANDLE();
		
		hFile = Kernel32.INSTANCE.CreateFile(
				windowsPath,
				WinNT.FILE_READ_DATA,
				WinNT.FILE_SHARE_READ,
				null,
				WinNT.OPEN_EXISTING,
				WinNT.FILE_ATTRIBUTE_NORMAL,
				null);
		
		if (hFile.equals(WinBase.INVALID_HANDLE_VALUE)) {
			int errno = Kernel32.INSTANCE.GetLastError();
			
			Win32Exception ex = new Win32Exception(errno);
			log.error("Unable to open file '"+windowsPath+"' for streaming", ex);
			
			switch (errno) {
			case Kernel32.ERROR_ACCESS_DENIED:
				throw new AccessDeniedException(windowsPath + ": " + ex.getMessage());
			case Kernel32.ERROR_FILE_NOT_FOUND:
				throw new FileNotFoundException(windowsPath + ": " + ex.getMessage());
			default:
				throw new IOException("Could not open file '"+windowsPath+"'", new Win32Exception(errno));
			}
		}
		
		ByteBuffer buf = ByteBuffer.allocate( (limit > 0) ? limit : READ_BUFFER_SIZE );
		
		IntByReference nbRead = new IntByReference();
		int totalRead = 0;
		while(true) {
			boolean success = Kernel32.INSTANCE.ReadFile(hFile, buf, buf.capacity(), nbRead, null);
			if ( ! success || nbRead.getValue() == 0 || totalRead >= limit) {
				break;
			} else {
				byte[] b = new byte[nbRead.getValue()];
				buf.get(b, 0, nbRead.getValue());

				os.write(b);
				buf.clear();
				
				totalRead += nbRead.getValue();
			}
		}

		if (!Kernel32.INSTANCE.CloseHandle(hFile)) {
			log.warn("Unable to close file handle", new Win32Exception(Kernel32.INSTANCE.GetLastError()));
		}
		
	}

}
