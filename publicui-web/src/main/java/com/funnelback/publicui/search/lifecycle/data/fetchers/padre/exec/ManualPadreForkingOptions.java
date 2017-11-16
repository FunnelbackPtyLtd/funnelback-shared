package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public class ManualPadreForkingOptions implements PadreForkingOptions {

    @Getter private final long padreForkingTimeout;
    
    @Getter private final int padreMaxPacketSize;
    
    @Getter private final int sizeAtWhichToCompressPackets;
}
