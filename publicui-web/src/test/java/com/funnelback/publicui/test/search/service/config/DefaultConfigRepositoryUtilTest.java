package com.funnelback.publicui.test.search.service.config;

import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import java.io.IOException;

public class DefaultConfigRepositoryUtilTest {

    @Test
    public void testCollectionDirectoryValidation() throws IOException {
        File searchHome = new File("src/test/resources/dummy-search_home").getCanonicalFile();

        Assert.assertTrue(DefaultConfigRepository.isValidCollectionConfigFolder(searchHome, new File(searchHome, "conf/collection1")));
        Assert.assertTrue(DefaultConfigRepository.isValidCollectionConfigFolder(searchHome, new File(searchHome, "local/implementations/implementation_name/configuration/collections/collection1")));

        Assert.assertFalse(DefaultConfigRepository.isValidCollectionConfigFolder(searchHome, new File(searchHome, "data/anything")));
        Assert.assertFalse(DefaultConfigRepository.isValidCollectionConfigFolder(searchHome, new File("/etc/passwd")));
    }
}

