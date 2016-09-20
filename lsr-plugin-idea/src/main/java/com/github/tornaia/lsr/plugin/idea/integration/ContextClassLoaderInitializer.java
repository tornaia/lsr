package com.github.tornaia.lsr.plugin.idea.integration;

import com.intellij.ide.plugins.cl.PluginClassLoader;

import java.util.logging.Logger;

/* https://issues.jboss.org/browse/SHRINKWRAP-246 Maven.resolve() is using the ThreadContextClassLoader
 * which is problematic in a modular/plugin environment it must be com.intellij.ide.plugins.cl.PluginClassLoader
 * but not com.intellij.util.lang.UrlClassLoader
 */
public final class ContextClassLoaderInitializer {

    private static Logger LOG = Logger.getLogger(ContextClassLoaderInitializer.class.getCanonicalName());

    public static void install(ClassLoader pluginClassLoader) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        LOG.fine("Old contextClassLoader: " + oldContextClassLoader);
        if (!(pluginClassLoader instanceof PluginClassLoader)) {
            throw new RuntimeException("Classloader is not PluginClassLoader: " + pluginClassLoader);
        }
        LOG.fine("New contextClassLoader: " + pluginClassLoader);
        Thread.currentThread().setContextClassLoader(pluginClassLoader);
    }
}
