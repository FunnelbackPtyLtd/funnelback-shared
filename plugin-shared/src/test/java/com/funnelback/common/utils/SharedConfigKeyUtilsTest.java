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

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List <Object> data() {
        return List.of(new Object[][] {
            {"with no wildcards", "hello.*.*", List.of(),  "hello.*.*"},
            {"with wildcard at start", "*.foo", List.of("a"), "a.foo"},
            {"with wildcard not at start", "**.foo.*", List.of("a"), "**.foo.a"},
            {"with wildcard at end", "foo.*", List.of("a"), "foo.a"},
            {"with wildcard not at end", "*.foo.**", List.of("a"), "a.foo.**"},
            {"with wildcard in middle", "foo.*.bar", List.of("a"), "foo.a.bar"},
            {"with many wildcards", "*.*.*foo*.*.bar.*", List.of("a", "b", "c", "d"), "a.b.*foo*.c.bar.d"},
            {"with single wildcard", "*", List.of("a"), "a"},
            {"with too many wildcards", "hello.*.h.*", List.of("a", "b", "c"), null}
        });
    }

    @Test
    public void test() {
        if (expectedKey == null) {
            exceptionRule.expect(RuntimeException.class);
            exceptionRule.expectMessage("Too many wildcards provided for key " + key);
        }
        Assert.assertEquals(expectedKey, SharedConfigKeyUtils.getKeyWithWildcard(key, wildcards));
    }
}
