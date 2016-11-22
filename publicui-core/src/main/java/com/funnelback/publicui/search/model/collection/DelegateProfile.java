package com.funnelback.publicui.search.model.collection;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * A delegate profile which can be extended to override values of the immutable Profile.
 * 
 * <p>Note Profile is not actually immutable but it should be treated as such</p>
 * <p>See {@link DelegateCollection} for an example of how to use this type
 * of class</p>
 */
@RequiredArgsConstructor
public class DelegateProfile extends Profile {

    @Delegate private final Profile profile;
    
}
