package com.funnelback.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SharedConfigKeyUtilsTest {

    private static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of("with no wildcards", "hello.*.*", List.of(),  "hello.*.*", 1),
            Arguments.of("with wildcard at start", "*.foo", List.of("a"), "a.foo", 0),
            Arguments.of("with wildcard not at start", "**.foo.*", List.of("a"), "**.foo.a", 0),
            Arguments.of("with wildcard at end", "foo.*", List.of("a"), "foo.a", 0),
            Arguments.of("with wildcard not at end", "*.foo.**", List.of("a"), "a.foo.**", 0),
            Arguments.of("with wildcard in middle", "foo.*.bar", List.of("a"), "foo.a.bar", 0),
            Arguments.of("with many wildcards", "*.*.*foo*.*.bar.*", List.of("a", "b", "c", "d"), "a.b.*foo*.c.bar.d", 1),
            Arguments.of("with single wildcard", "*", List.of("a"), "a", 0),
            Arguments.of("with too many wildcards", "hello.*.h.*", List.of("a", "b", "c"), null, 0),
            Arguments.of("with dot at start", ".foo", List.of(), ".foo", 2),
            Arguments.of("with dot at end", "foo.", List.of(), "foo.", 3)
        );
    }

    @DisplayName("Substitute key")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void test(String desc, String key, List<String> wildcards, String expectedKey, int isValid) {
        if (isValid != 0) {
            String expectMessage = null;
            switch (isValid) {
                case 1:
                    expectMessage= "Plugin key pattern '" + key + "' may not contain consecutive wildcards";
                    break;
                case 2:
                    expectMessage = "Plugin key pattern '" + key + "' cannot start with '.'";
                    break;
                case 3:
                    expectMessage = "Plugin key pattern '" + key + "' cannot end with '.'";
                    break;
            }
            var exception = Assertions.assertThrows(IllegalArgumentException.class, () -> SharedConfigKeyUtils.validateKey(key));
            Assertions.assertEquals(expectMessage, exception.getMessage());
        } else if (expectedKey == null) {
            var exception = assertThrows(RuntimeException.class, () -> SharedConfigKeyUtils.getKeyWithWildcard(key, wildcards));
            assertEquals("Too many wildcards provided for key " + key, exception.getMessage());
        } else {
            assertEquals(expectedKey, SharedConfigKeyUtils.getKeyWithWildcard(key, wildcards));
        }
    }
}
