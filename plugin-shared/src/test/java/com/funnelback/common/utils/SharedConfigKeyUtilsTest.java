package com.funnelback.common.utils;

import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class SharedConfigKeyUtilsTest {
    @Rule public ExpectedException exceptionRule = ExpectedException.none();

    private final String desc;
    private final String key;
    private final List<String> wildcards;
    private final String expectedKey;
    private final int isValid;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List <Object> data() {
        return List.of(new Object[][] {
            {"with no wildcards", "hello.*.*", List.of(),  "hello.*.*", 1},
            {"with wildcard at start", "*.foo", List.of("a"), "a.foo", 0},
            {"with wildcard not at start", "**.foo.*", List.of("a"), "**.foo.a", 0},
            {"with wildcard at end", "foo.*", List.of("a"), "foo.a", 0},
            {"with wildcard not at end", "*.foo.**", List.of("a"), "a.foo.**", 0},
            {"with wildcard in middle", "foo.*.bar", List.of("a"), "foo.a.bar", 0},
            {"with many wildcards", "*.*.*foo*.*.bar.*", List.of("a", "b", "c", "d"), "a.b.*foo*.c.bar.d", 1},
            {"with single wildcard", "*", List.of("a"), "a", 0},
            {"with too many wildcards", "hello.*.h.*", List.of("a", "b", "c"), null, 0},
            {"with dot at start", ".foo", List.of(), ".foo", 2},
            {"with dot at end", "foo.", List.of(), "foo.", 3},
        });
    }

    @Test
    public void testKeyWildWildcard() {
        if (expectedKey == null) {
            exceptionRule.expect(RuntimeException.class);
            exceptionRule.expectMessage("Too many wildcards provided for key " + key);
        }
        Assert.assertEquals(expectedKey, SharedConfigKeyUtils.getKeyWithWildcard(key, wildcards));
    }

    @Test
    public void testValidKeyPattern() {
        if (isValid != 0) {
            exceptionRule.expect(IllegalArgumentException.class);
            switch (isValid) {
                case 1:
                    exceptionRule.expectMessage("Plugin key pattern '" + key + "' may not contain consecutive wildcards");
                    break;
                case 2:
                    exceptionRule.expectMessage("Plugin key pattern '" + key + "' cannot start with '.'");
                    break;
                case 3:
                    exceptionRule.expectMessage("Plugin key pattern '" + key + "' cannot end with '.'");
                    break;
            }
        }
        SharedConfigKeyUtils.validateKey(key);
    }
}
