package com.funnelback.publicui.test.mock;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.UserKeysMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MockUserKeysMapper implements UserKeysMapper {

    @Getter
    private boolean traversed = false;
    
    @Override
    public List<String> getUserKeys(Collection c, SearchTransaction transaction) {
        traversed = true;
        return Arrays.asList(new String[] {MockUserKeysMapper.class.getSimpleName()});
    }

}
