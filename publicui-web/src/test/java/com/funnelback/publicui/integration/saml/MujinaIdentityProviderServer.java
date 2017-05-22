package com.funnelback.publicui.integration.saml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.TeeOutputStream;

import com.funnelback.common.wait.WaitFor;

public class MujinaIdentityProviderServer {
    private ExecuteWatchdog watchdog;
    
    public void start() throws Exception {
        String jvm_location;
        if (System.getProperty("os.name").startsWith("Win")) {
            jvm_location = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe";
        } else {
            jvm_location = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        }
        
        File mujinaIdpJar = new File("target/mujina-idp-dependency/mujina-idp-5.0.5.jar");
        if (!mujinaIdpJar.exists()) {
            throw new IllegalStateException("No mujina-idp .jar in target, maybe because you're running in an IDE? "
                + "If so, you need to run maven to put it there first!");
        }
        
        CommandLine cmdLine = new CommandLine(jvm_location);
        cmdLine.addArgument("-jar");
        cmdLine.addArgument(mujinaIdpJar.getAbsolutePath());

        ByteArrayOutputStream mujinaOutput = new ByteArrayOutputStream();
        OutputStream teeStream = new TeeOutputStream(System.out, mujinaOutput);
        
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(teeStream));
        executor.execute(cmdLine, resultHandler);
        
        // Wait for mujina to be ready to accept connections
        try {
            new WaitFor(60 * 1000, 1000) {
                @Override
                public void step() { }
                
                @Override
                public boolean condition() {
                    return new String(mujinaOutput.toByteArray(), StandardCharsets.ISO_8859_1).contains("Started MujinaIdpApplication");
                }
            }.setDescription("Waiting for Mujina IdP to start")
            .waitForConditionToHappen();
        } catch (Exception e) {
            try {
                this.stop();
            } catch (Exception e1) {
                // Oh well - We tried - Throw the original execption I guess
            }
            throw e;
        }
    }

    public void stop() throws Exception {
        watchdog.destroyProcess();
    }

    public String getBaseUrl() {
        // Mujinda always runs on port 8080 - Doesn't work on anything else yet
        return "http://localhost:8080";
    }
}
