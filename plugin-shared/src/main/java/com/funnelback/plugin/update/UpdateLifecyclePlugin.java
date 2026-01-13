package com.funnelback.plugin.update;

/**
 * Plugin interface for hooking into collection update lifecycle events.
 * 
 * <p>Allows plugins to execute custom logic at specific points during the collection update process,
 * such as before gathering, after gathering, on failures, or when the update is stopped.
 * 
 * <p>All methods are default implementations, allowing implementers to selectively override only
 * the lifecycle events they need. This design choice prevents implementers from having to provide
 * empty implementations for events they don't care about, reducing boilerplate code.
 * 
 * <p>Multiple plugins can be registered for the same lifecycle event. Execution order is
 * controlled via the {@link #priority()} method, with lower values executing first.
 * 
 * @see UpdateLifecycleContext
 */
public interface UpdateLifecyclePlugin {

    /**
     * Invoked after the swaping views phase completes successfully.
     *
     * <p>This method only executes if swaping view completes without errors. For handling
     * failure scenarios, implement {@link #onFail(UpdateLifecycleContext)} instead.
     *
     * @param context provides access to collection configuration and plugin configuration files
     * @throws Exception if the plugin fails to execute post-swap logic. Exceptions will
     *                   typically cause the update to fail, preventing indexing from proceeding.
     */
    default void onPostSwap(UpdateLifecycleContext context) throws Exception {}

    /**
     * Invoked when the update process fails at any stage.
     * 
     * @param context provides access to collection configuration and plugin configuration files
     * @throws Exception if the plugin fails during error handling. These exceptions are
     *                   typically logged but do not prevent other plugins' onFail hooks
     *                   from executing.
     */
    default void onFail(UpdateLifecycleContext context) throws Exception {}

    /**
     * Invoked when the update process is manually stopped or interrupted.
     * 
     * @param context provides access to collection configuration and plugin configuration files
     * @throws Exception if the plugin fails during stop handling. These exceptions are
     *                   typically logged but do not prevent shutdown from proceeding.
     */
    default void onStop(UpdateLifecycleContext context) throws Exception {}

    /**
     * Determines execution order when multiple plugins are registered for the same lifecycle event.
     * 
     * <p>Priority controls the sequence in which plugins execute within each lifecycle phase.
     * Lower values execute first, allowing plugins to establish ordering dependencies when needed.
     * 
     * <p>The default priority of 100 provides a middle ground, allowing other plugins to
     * easily execute before (priority &lt; 100) or after (priority &gt; 100) plugins that
     * don't override this method.
     * 
     * <p>Priority is important when plugins have dependencies on each other's side effects,
     * such as when one plugin must modify configuration before another plugin reads it.
     * 
     * @return the execution priority, where lower numbers execute first. Default is 100.
     */
    default int priority() {
        return 100;
    }
}