package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor.ExecutionException;

/**
 * Forks PADRE using Windows Native calls, in order to use impersonation.
 */
@RequiredArgsConstructor
public class WindowsNativePadreForker implements PadreForker {

    private final I18n i18n;
    private final int execTimeout;
    
    @Override
    public ExecutionReturn execute(String commandLine, Map<String, String> environment)
        throws PadreForkingException {

        try {
            return new WindowsNativeExecutor(i18n, execTimeout).execute(commandLine, environment);
        } catch (ExecutionException ee) {
            throw new PadreForkingException(ee);
        }
    }
    

}
