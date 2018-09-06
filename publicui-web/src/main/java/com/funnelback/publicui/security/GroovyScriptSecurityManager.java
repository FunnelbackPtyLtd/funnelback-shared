package com.funnelback.publicui.security;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Permission;
import java.util.stream.Stream;

/**
 * A security manager which prevents implementation groovy scripts from
 * doing certain things which could cause problems.
 *
 * Currently:
 * * Calling System.exit() (which would stop jetty)
 *
 * Perhaps in the future:
 * * Accessing the local file system (though maybe we'll need some entitlement system for some cases)
 * * Accessing the network
 * * Executing other processes
 * * Running for too long (though it's not to clear how we'll achieve that)
 */
public class GroovyScriptSecurityManager  extends SecurityManager {
    @Getter
    private int attemptedExitCode;

    private SecurityManager parentSecurityManager;

    public GroovyScriptSecurityManager(SecurityManager parentSecurityManager) {
        this.parentSecurityManager = parentSecurityManager;
    }

    @Override
    public void checkPermission(Permission permission) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(permission);
        }
    }

    @Override
    public void checkExit(int code) {
        attemptedExitCode = code;
        throw new SecurityException("Preventing System.exit call with attempted exit code " + attemptedExitCode);
    }
}